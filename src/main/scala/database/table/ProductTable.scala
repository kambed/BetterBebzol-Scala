package database.table

import model.domain.Product
import slick.jdbc.MySQLProfile.api._

class ProductTable(tag: Tag) extends Table[Product](tag, "Product") {
  private lazy val users = TableQuery[UserTable]

  def productId = column[Long]("product_id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def productName = column[String]("product_name")

  def calories = column[Int]("calories")

  def protein = column[Option[Double]]("protein")

  def fat = column[Option[Double]]("fat")

  def carbohydrates = column[Option[Double]]("carbohydrates")

  def user = foreignKey("user", userId, users)(_.userId)

  def * = (productId, userId, productName, calories, protein, fat, carbohydrates).mapTo[Product]
}
