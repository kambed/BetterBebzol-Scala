package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.{MealProductTable, MealTable, ProductTable}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode400
import model.command.product.CreateProductCommand
import model.domain.Product
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
    insertProduct(command.toProduct).onComplete {
      case scala.util.Success(product) =>
        val response = Command(ReturnCommand(product))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case scala.util.Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  //=====DATABASE METHODS===========================================================

  private def getAllProducts: Future[Seq[Product]] = {
    MySQLConnection.db.run(productTable.result)
  }

  private def insertProduct(product: Product): Future[Product] = {
    MySQLConnection.db.run((productTable returning productTable.map(_.productId) into ((product, productId) => product.copy(productId = productId))) += product)
  }
}