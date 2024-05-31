package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.{MealProductTable, MealTable, ProductTable}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode403}
import model.command.product.CreateProductCommand
import model.domain.{Meal, MealProduct, Product}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object ProductRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new ProductRepository(context))
}

private class ProductRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  lazy val mealTable = TableQuery[MealTable]
  lazy val productTable = TableQuery[ProductTable]
  lazy val mealProductTable = TableQuery[MealProductTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createProductCommand: CreateProductCommand => handleCreateProductCommand(createProductCommand, msg)
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

  private def getMealById(id: Long): Future[Option[Meal]] = {
    MySQLConnection.db.run(mealTable.filter(_.mealId === id).result.headOption)
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

}