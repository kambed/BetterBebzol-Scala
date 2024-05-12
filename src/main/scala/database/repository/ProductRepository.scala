package database.repository

import database.connection.MySQLConnection
import model.domain.Product
import slick.jdbc.MySQLProfile.api.*
import slick.lifted.TableQuery
import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

trait ProductRepositoryComponent {
  val productRepository: ProductRepository

  class ProductRepository {
    lazy val table = TableQuery[ProductTable]

    def getAllProducts: Future[Seq[Product]] = {
      MySQLConnection.db.run(table.result)
    }

    def insertProduct(product: Product): Future[Product] = {
      MySQLConnection.db.run((table returning table.map(_.id)) += product).map(id => product.copy(id = id))
    }
  }
}

class ProductTable(tag: Tag) extends Table[Product](tag, "product") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def calories = column[Int]("calories")

  def * = (id, name, calories).mapTo[Product]
}