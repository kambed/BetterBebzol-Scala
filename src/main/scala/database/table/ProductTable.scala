package database.table

import model.domain.Product
import slick.jdbc.MySQLProfile.api._

class ProductTable(tag: Tag) extends Table[Product](tag, "Product") {

  def productId = column[Long]("product_id", O.PrimaryKey, O.AutoInc)

  def productName = column[String]("product_name")

  def calories = column[Option[Double]]("calories")

  def protein = column[Option[Double]]("protein")

  def fat = column[Option[Double]]("fat")

  def carbohydrates = column[Option[Double]]("carbohydrates")

  def * = (productId, productName, calories, protein, carbohydrates, fat).mapTo[Product]
}
