package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.ProductTable
import model.command.abstracts.{Command, ReturnCommand}
import model.command.product.{CreateProductCommand, ListAllProductsCommand}
import model.domain.Product
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object ProductRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new ProductRepository(context))
}

private class ProductRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  lazy val table = TableQuery[ProductTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createProductCommand: CreateProductCommand =>
        insertProduct(createProductCommand.toProduct).onComplete(product => msg.replyTo ! Command(ReturnCommand(product.get)))
      case _: ListAllProductsCommand =>
        getAllProducts.onComplete(products => msg.replyTo ! Command(ReturnCommand(products.get)))
    }
    this
  }

  private def getAllProducts: Future[Seq[Product]] = {
    MySQLConnection.db.run(table.result)
  }

  private def insertProduct(product: Product): Future[Product] = {
    MySQLConnection.db.run((table returning table.map(_.productId)) += product).map(id => product.copy(productId = id))
  }
}