package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.ProductTable
import model.command.{CreateProductCommand, ListAllProductsCommand}
import model.command.abstracts.Command
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
        insertProduct(createProductCommand.toProduct).onComplete(msg.replyTo ! _.get)
        this
      case _: ListAllProductsCommand =>
        getAllProducts.onComplete(msg.replyTo ! _.get)
        this
    }
  }

  private def getAllProducts: Future[Seq[Product]] = {
    MySQLConnection.db.run(table.result)
  }

  private def insertProduct(product: Product): Future[Product] = {
    MySQLConnection.db.run((table returning table.map(_.id)) += product).map(id => product.copy(id = id))
  }
}