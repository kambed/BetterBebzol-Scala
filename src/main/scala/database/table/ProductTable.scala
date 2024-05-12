package database.table

import slick.jdbc.MySQLProfile.api.*
import model.domain.Product

class ProductTable(tag: Tag) extends Table[Product](tag, "product") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def calories = column[Int]("calories")

  def * = (id, name, calories).mapTo[Product]
}
