package database.table

import model.domain.MealType.MealType
import model.domain.{Meal, MealType}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.MySQLProfile.api._

class MealTable(tag: Tag) extends Table[Meal](tag, "Meal") {
  def mealId = column[Long]("meal_id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("user_id")

  def mealType = column[MealType]("meal_type")

  def calories = column[Float]("calories")

  def protein = column[Float]("protein")

  def fat = column[Float]("fat")

  def carbohydrates = column[Float]("carbohydrates")

  def date = column[String]("date")

  def * = (mealId, userId, mealType, calories, protein, carbohydrates, fat, date).mapTo[Meal]

  implicit val mealTypeMapper: JdbcType[MealType] with BaseTypedType[MealType] = MappedColumnType.base[MealType, String](
    e => e.toString,
    s => MealType.withName(s)
  )
}
