package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.{MealProductTable, MealTable, ProductTable}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode403, ExceptionWithResponseCode404}
import model.command.product._
import model.domain.{Meal, MealProduct, Product}
import model.dto.{MealProductDto, ProductQuantityDto}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ProductRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new ProductRepository(context))
}

private class ProductRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  private lazy val mealTable = TableQuery[MealTable]
  private lazy val productTable = TableQuery[ProductTable]
  private lazy val mealProductTable = TableQuery[MealProductTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createProductCommand: CreateProductCommand => handleCreateProductCommand(createProductCommand, msg)
      case editProductCommand: EditProductCommand => handleEditProductCommand(editProductCommand, msg)
      case getProductByIdCommand: GetProductByIdCommand => handleGetProductCommand(getProductByIdCommand, msg)
      case getMealProductsByIdCommand: GetMealProductsByIdCommand => handleGetMealProductCommand(getMealProductsByIdCommand, msg)
      case deleteProductByIdCommand: DeleteProductByIdCommand => handleDeleteProductByIdCommand(deleteProductByIdCommand, msg)
      case _ => msg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400("Invalid command")))
    }
    this
  }

  //=====COMMAND HANDLERS===========================================================
  private def handleCreateProductCommand(command: CreateProductCommand, originalMsg: Command): Unit = {
    (for {
      meal <- getMealById(command.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      insertedProduct <- insertProduct(command.toProduct)
      updatedMeal <- updateMeal(mealWithCheck, updateMealByProduct(mealWithCheck, insertedProduct, command.quantity.getOrElse(1)))
      insertedMealProduct <- insertMealProduct(updatedMeal.mealId, insertedProduct.productId, command.quantity.getOrElse(1))
    } yield (insertedProduct, updatedMeal, insertedMealProduct)).onComplete({
      case Success((product, _, _)) =>
        val response = Command(ReturnCommand(product))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    })
  }

  private def handleEditProductCommand(command: EditProductCommand, originalMsg: Command): Unit = {
    (for {
      mealProduct <- getMealProductByProductId(command.productId)
      meal <- getMealById(mealProduct.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      product <- getProductById(mealProduct.productId)
      updatedProduct <- updateProduct(product, command.toProduct)
      updatedMeal <- updateMeal(mealWithCheck, updateMealByProduct(removeFromMealProduct(mealWithCheck, product, mealProduct.quantity), updatedProduct, command.quantity.getOrElse(1)))
      updatedMealProduct <- updateMealProduct(mealProduct.copy(quantity = command.quantity.getOrElse(1)))
    } yield (updatedProduct, updatedMeal, updatedMealProduct)).onComplete({
      case Success((product, _, _)) =>
        val response = Command(ReturnCommand(product))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    })
  }

  private def handleGetProductCommand(command: GetProductByIdCommand, originalMsg: Command): Unit = {
    (for {
      mealProduct <- getMealProductByProductId(command.productId)
      meal <- getMealById(mealProduct.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      product <- getProductById(mealProduct.productId)
    } yield (product, mealWithCheck)).onComplete({
      case Success((product, _)) =>
        val response = Command(ReturnCommand(product))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    })
  }

  private def handleGetMealProductCommand(command: GetMealProductsByIdCommand, originalMsg: Command): Unit = {
    (for {
      meal <- getMealById(command.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      mealProducts <- getMealProductsByMealId(mealWithCheck.mealId)
      products <- getProductsByIdList(mealProducts.map(_.productId))
    } yield (products, mealProducts, mealWithCheck)).onComplete {
      case Success((products, mealProducts, meal)) =>
        val mealProductDto = MealProductDto(meal.toMealDto, mapProductsWithQuantityToProductDto(products, mealProducts))
        val response = Command(ReturnCommand(mealProductDto))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleDeleteProductByIdCommand(command: DeleteProductByIdCommand, originalMsg: Command): Unit = {
    (for {
      mealProduct <- getMealProductByProductId(command.productId)
      meal <- getMealById(mealProduct.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      product <- getProductById(mealProduct.productId)
      updateMeal <- updateMeal(mealWithCheck, removeFromMealProduct(mealWithCheck, product, mealProduct.quantity))
      deleteMealProduct <- deleteMealProductByProductId(command.productId)
      deleteProduct <- deleteProductById(command.productId)
    } yield (product, updateMeal, deleteMealProduct, deleteProduct)).onComplete {
      case Success((product, _, _, _)) =>
        val response = Command(ReturnCommand(product))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def mapProductsWithQuantityToProductDto(products: Seq[Product], mealProducts: Seq[MealProduct]) = {
    val productIdToQuantity: Map[Long, Int] = mealProducts.map(mp => mp.productId -> mp.quantity).toMap
    products.map { product =>
      val quantity = productIdToQuantity.getOrElse(product.productId, 1)
      ProductQuantityDto(
        productId = product.productId,
        productName = product.productName,
        calories = product.calories,
        proteins = product.proteins,
        fat = product.fat,
        carbohydrates = product.carbohydrates,
        quantity = quantity
      )
    }
  }

  private def checkIfUserIsOwnerOfMeal(userId: Long, meal: Meal): Future[Meal] = {
    if (meal.userId != userId) {
      return Future.failed(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${meal.mealId}"))
    }
    Future.successful(meal)
  }

  private def updateMealByProduct(meal: Meal, product: Product, quantity: Int): Meal = {
    meal.copy(
      calories = meal.calories + (product.calories.getOrElse(0.0) * quantity),
      proteins = meal.proteins + (product.proteins.getOrElse(0.0) * quantity),
      fat = meal.fat + (product.fat.getOrElse(0.0) * quantity),
      carbohydrates = meal.carbohydrates + (product.carbohydrates.getOrElse(0.0) * quantity)
    )
  }

  private def removeFromMealProduct(meal: Meal, product: Product, quantity: Int): Meal = {
    meal.copy(
      calories = meal.calories - (product.calories.getOrElse(0.0) * quantity),
      proteins = meal.proteins - (product.proteins.getOrElse(0.0) * quantity),
      fat = meal.fat - (product.fat.getOrElse(0.0) * quantity),
      carbohydrates = meal.carbohydrates - (product.carbohydrates.getOrElse(0.0) * quantity)
    )
  }

  //=====DATABASE METHODS===========================================================
  private def getProductById(id: Long): Future[Product] = {
    MySQLConnection.db.run(productTable.filter(_.productId === id).result.headOption).flatMap {
      case Some(product) => Future.successful(product)
      case None => Future.failed(ExceptionWithResponseCode404(s"Product with id $id not found"))
    }
  }

  private def getProductsByIdList(ids: Seq[Long]): Future[Seq[Product]] = {
    MySQLConnection.db.run(productTable.filter(_.productId inSet ids).result)
  }

  private def insertProduct(product: Product): Future[Product] = {
    MySQLConnection.db.run((productTable returning productTable.map(_.productId) into ((product, productId) => product.copy(productId = productId))) += product)
  }

  private def updateProduct(oldProduct: Product, product: Product): Future[Product] = {
    val modifiedProduct = product.copy(productId = oldProduct.productId)
    MySQLConnection.db.run(productTable.filter(_.productId === product.productId).update(modifiedProduct)).map(_ => modifiedProduct)
  }

  private def deleteProductById(productId: Long): Future[Int] = {
    MySQLConnection.db.run(productTable.filter(_.productId === productId).delete)
  }

  private def getMealById(id: Long): Future[Meal] = {
    MySQLConnection.db.run(mealTable.filter(_.mealId === id).result.headOption).flatMap {
      case Some(meal) => Future.successful(meal)
      case None => Future.failed(ExceptionWithResponseCode404(s"Meal with id $id not found"))
    }
  }

  private def updateMeal(oldMeal: Meal, meal: Meal): Future[Meal] = {
    val modifiedMeal = meal.copy(mealId = oldMeal.mealId)
    MySQLConnection.db.run(mealTable.filter(_.mealId === meal.mealId).update(modifiedMeal)).map(_ => modifiedMeal)
  }

  private def getMealProductsByMealId(mealId: Long): Future[Seq[MealProduct]] = {
    MySQLConnection.db.run(mealProductTable.filter(_.mealId === mealId).result)
  }

  private def getMealProductByProductId(productId: Long): Future[MealProduct] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === productId).result.headOption).flatMap {
      case Some(mealProduct) => Future.successful(mealProduct)
      case None => Future.failed(ExceptionWithResponseCode404(s"Product with id $productId is not in any meal"))
    }
  }

  private def insertMealProduct(mealId: Long, productId: Long, quantity: Int): Future[Int] = {
    val mealProduct = MealProduct(mealId, productId, quantity)
    MySQLConnection.db.run(mealProductTable += mealProduct)
  }

  private def updateMealProduct(mealProduct: MealProduct): Future[Option[MealProduct]] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === mealProduct.productId).update(mealProduct)).map {
      case 0 => None
      case _ => Some(mealProduct)
    }
  }

  private def deleteMealProductByProductId(productId: Long): Future[Int] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === productId).delete)
  }
}