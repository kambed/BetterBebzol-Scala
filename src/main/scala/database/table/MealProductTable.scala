package database.table

import model.domain.{Meal, MealProduct, Product}
import slick.jdbc.MySQLProfile.api._
import slick.lifted.{ForeignKeyQuery, ProvenShape}

class MealProductTable(tag: Tag) extends Table[MealProduct](tag, "MealProduct") {
  def mealId: Rep[Long] = column[Long]("meal_id")
  def productId: Rep[Long] = column[Long]("product_id")
  def quantity: Rep[Int] = column[Int]("quantity")

  // Primary key
  def pk = primaryKey("pk_meal_product", (mealId, productId))

  // Foreign keys
  def meal: ForeignKeyQuery[MealTable, Meal] = foreignKey("fk_meal", mealId, TableQuery[MealTable])(_.mealId)
  def product: ForeignKeyQuery[ProductTable, Product] = foreignKey("fk_product", productId, TableQuery[ProductTable])(_.productId)

  // Projection mapping
  def * : ProvenShape[MealProduct] = (mealId, productId, quantity).mapTo[MealProduct]
}