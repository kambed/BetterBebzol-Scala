package database.table

import model.domain.User
import model.domain.enums.UserActivity.HowActive
import model.domain.enums.UserSex.Sex
import model.domain.enums.{UserActivity, UserSex}
import slick.ast.BaseTypedType
import slick.jdbc.JdbcType
import slick.jdbc.MySQLProfile.api._
import slick.lifted.ProvenShape

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  def userId = column[Long]("user_id", O.PrimaryKey, O.AutoInc)

  def email = column[String]("email")

  def password = column[String]("password")

  def sex = column[Option[Sex]]("sex")

  def age = column[Option[Int]]("age")

  def height = column[Option[Int]]("height")

  def weight = column[Option[Int]]("weight")

  def howActive = column[Option[HowActive]]("how_active")

  def * : ProvenShape[User] = (userId, email, password, sex, age, height, weight, howActive).mapTo[User]

  implicit val howActiveMapper: JdbcType[HowActive] with BaseTypedType[HowActive] = MappedColumnType.base[HowActive, String](
    e => e.toString,
    s => UserActivity.withName(s)
  )

  implicit val sexMapper: JdbcType[Sex] with BaseTypedType[Sex] = MappedColumnType.base[Sex, String](
    e => e.toString,
    s => UserSex.withName(s)
  )
}
