package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.{MealProductTable, MealTable, ProductTable}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode403}
import model.command.product.{CreateProductCommand, DeleteProductByIdCommand, EditProductCommand, GetMealProductsByIdCommand, GetProductByIdCommand}
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
    checkIfUserIsOwnerOfMeal(command.userId, command.mealId).onComplete {
      case Success(isOwner) =>
        if (!isOwner) {
          originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${command.mealId}")))
          return
        }
        getMealById(command.mealId).onComplete {
          case Success(Some(meal)) =>
            insertProduct(command.toProduct).onComplete {
              case Success(insertedProduct) =>
                val quantity = command.quantity.getOrElse(1)
                val updatedMeal = updateMealByProduct(meal, insertedProduct, quantity)
                updateMeal(updatedMeal).onComplete {
                  case Success(Some(updatedMeal)) =>
                    insertMealProduct(updatedMeal.mealId, insertedProduct.productId, quantity).onComplete {
                      case Success(_) =>
                        val response = Command(ReturnCommand(insertedProduct))
                        response.addAllDelayedRequests(originalMsg.delayedRequests)
                        originalMsg.replyTo ! response
                      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                    }
                  case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                }
              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
            }
        }

      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleEditProductCommand(command: EditProductCommand, originalMsg: Command): Unit = {
    getMealProductByProductId(command.productId).onComplete {
      case Success(Some(mealProduct)) =>
        checkIfUserIsOwnerOfMeal(command.userId, mealProduct.mealId).onComplete {
          case Success(isOwner) =>
            if (!isOwner) {
              originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with product id ${command.productId}")))
              return
            }
            getProductById(command.productId).onComplete {
              case Success(Some(product)) =>
                val updatedProduct = command.toProduct
                updateProduct(updatedProduct).onComplete {
                  case Success(Some(updatedProduct)) =>
                    getMealById(mealProduct.mealId).onComplete {
                      case Success(Some(meal)) =>
                        val remMeal = removeFromMealProduct(meal, product, mealProduct.quantity)
                        val updatedMeal = updateMealByProduct(remMeal, updatedProduct, command.quantity.getOrElse(1))
                        updateMeal(updatedMeal).onComplete {
                          case Success(Some(_)) =>
                            updateMealProduct(updateMealProductQuantity(mealProduct, command.quantity.getOrElse(1))).onComplete {
                              case Success(Some(_)) =>
                                val response = Command(ReturnCommand(updatedProduct))
                                response.addAllDelayedRequests(originalMsg.delayedRequests)
                                originalMsg.replyTo ! response
                              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                            }
                          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                        }
                      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                    }
                  case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                }
              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
            }
          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      case Success(None) => originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"Product with id ${command.productId} is not in any meal")))
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetProductCommand(command: GetProductByIdCommand, originalMsg: Command): Unit = {
    getMealProductByProductId(command.productId).onComplete {
      case Success(Some(mealProduct)) =>
        checkIfUserIsOwnerOfMeal(command.userId, mealProduct.mealId).onComplete {
          case Success(isOwner) =>
            if (!isOwner) {
              originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with product id ${command.productId}")))
              return
            }
            getProductById(command.productId).onComplete {
              case Success(Some(product)) =>
                val response = Command(ReturnCommand(product))
                response.addAllDelayedRequests(originalMsg.delayedRequests)
                originalMsg.replyTo ! response
              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
            }
          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      case Success(None) => originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"Product with id ${command.productId} is not in any meal")))
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetMealProductCommand(command: GetMealProductsByIdCommand, originalMsg: Command): Unit = {
    checkIfUserIsOwnerOfMeal(command.userId, command.mealId).onComplete {
      case Success(isOwner) =>
        if (!isOwner) {
          originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${command.mealId}")))
          return
        }
        getMealProductsByMealId(command.mealId).onComplete {
          case Success(mealProducts) =>
            getMealById(command.mealId).onComplete {
              case Success(Some(meal)) =>
                getProductsByIdList(mealProducts.map(_.productId)).onComplete {
                  case Success(products) =>
                    val mealProductDto = MealProductDto(meal.toMealDto, mapProductsWithQuantityToProductDto(products, mealProducts))
                    val response = Command(ReturnCommand(mealProductDto))
                    response.addAllDelayedRequests(originalMsg.delayedRequests)
                    originalMsg.replyTo ! response
                  case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                }

              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
            }
          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleDeleteProductByIdCommand(command: DeleteProductByIdCommand, originalMsg: Command): Unit = {
    getMealProductByProductId(command.productId).onComplete {
      case Success(Some(mealProduct)) => {
        checkIfUserIsOwnerOfMeal(command.userId, mealProduct.mealId).onComplete {
          case Success(isOwner) =>
            if (!isOwner) {
              originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with product id ${command.productId}")))
              return
            }
            getMealById(mealProduct.mealId).onComplete {
              case Success(Some(meal)) => {
                getProductById(command.productId).onComplete {
                  case Success(Some(product)) => {
                    val remMeal = removeFromMealProduct(meal, product, mealProduct.quantity)
                    updateMeal(remMeal).onComplete {
                      case Success(Some(_)) => {
                        deleteMealProductByProductId(command.productId).onComplete {
                          case Success(_) => {
                            deleteProductById(command.productId).onComplete {
                              case Success(_) => {
                                val response = Command(ReturnCommand(product))
                                response.addAllDelayedRequests(originalMsg.delayedRequests)
                                originalMsg.replyTo ! response
                              }
                              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                            }
                          }
                          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                        }
                      }
                      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                    }
                  }
                  case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
                }
              }
              case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
            }

          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      }
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

  //=====DATABASE METHODS===========================================================

  private def insertProduct(product: Product): Future[Product] = {
    MySQLConnection.db.run((productTable returning productTable.map(_.productId) into ((product, productId) => product.copy(productId = productId))) += product)
  }

  private def insertMealProduct(mealId: Long, productId: Long, quantity: Int): Future[Int] = {
    val mealProduct = MealProduct(mealId, productId, quantity)
    MySQLConnection.db.run(mealProductTable += mealProduct)
  }

  private def updateMeal(meal: Meal): Future[Option[Meal]] = {
    MySQLConnection.db.run(mealTable.filter(_.mealId === meal.mealId).update(meal)).map {
      case 0 => None
      case _ => Some(meal)
    }
  }

  private def deleteProductById(productId: Long): Future[Int] = {
    MySQLConnection.db.run(productTable.filter(_.productId === productId).delete)
  }

  private def deleteMealProductByProductId(productId: Long): Future[Int] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === productId).delete)
  }

  private def updateMealProduct(mealProduct: MealProduct): Future[Option[MealProduct]] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === mealProduct.productId).update(mealProduct)).map {
      case 0 => None
      case _ => Some(mealProduct)
    }
  }

  private def updateProduct(product: Product): Future[Option[Product]] = {
    MySQLConnection.db.run(productTable.filter(_.productId === product.productId).update(product)).map {
      case 0 => None
      case _ => Some(product)
    }
  }

  private def getMealById(id: Long): Future[Option[Meal]] = {
    MySQLConnection.db.run(mealTable.filter(_.mealId === id).result.headOption)
  }

  private def getProductById(id: Long): Future[Option[Product]] = {
    MySQLConnection.db.run(productTable.filter(_.productId === id).result.headOption)
  }

  private def getProductsByIdList(ids: Seq[Long]): Future[Seq[Product]] = {
    MySQLConnection.db.run(productTable.filter(_.productId inSet ids).result)
  }

  private def getMealProductByProductId(productId: Long): Future[Option[MealProduct]] = {
    MySQLConnection.db.run(mealProductTable.filter(_.productId === productId).result.headOption)
  }

  private def getMealProductsByMealId(mealId: Long): Future[Seq[MealProduct]] = {
    MySQLConnection.db.run(mealProductTable.filter(_.mealId === mealId).result)
  }

  private def getAllProducts: Future[Seq[Product]] = {
    MySQLConnection.db.run(productTable.result)
  }

  //=====LOGIC METHODS===========================================================
  private def checkIfUserIsOwnerOfMeal(userId: Long, mealId: Long): Future[Boolean] = {
    getMealById(mealId).map {
      case Some(meal) => meal.userId == userId
      case None => false
    }
  }

  private def updateMealByProduct(meal: Meal, product: Product, quantity: Int): Meal = {
    meal.copy(
      calories = meal.calories + (product.calories.getOrElse(0.0) * quantity),
      proteins = meal.proteins + (product.proteins.getOrElse(0.0) * quantity),
      fat = meal.fat + (product.fat.getOrElse(0.0) * quantity),
      carbohydrates = meal.carbohydrates + (product.carbohydrates.getOrElse(0.0) * quantity)
    )
  }

  private def updateMealProductQuantity(mealProduct: MealProduct, quantity: Int): MealProduct = {
    mealProduct.copy(
      quantity = quantity
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

}