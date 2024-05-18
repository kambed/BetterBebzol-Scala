package database.table

import model.domain.UserActivity.HowActive
import model.domain.{User, UserActivity}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def user_id = column[Long]("user_id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def password = column[String]("password")

  def age = column[Option[Int]]("age")

  def height = column[Option[Double]]("height")

  def weight = column[Option[Double]]("weight")

  def howActive = column[HowActive]("how_active")

  def * : ProvenShape[User] = (user_id, email, password, age, height, weight, howActive).mapTo[User]

  implicit val howActiveMapper: JdbcType[HowActive] with BaseTypedType[HowActive] = MappedColumnType.base[HowActive, String](
    e => e.toString,
    s => UserActivity.withName(s)
  )
}
