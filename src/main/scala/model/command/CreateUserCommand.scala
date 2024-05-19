package model.command

import model.command.abstracts.BaseCommand
import model.domain.{User, UserActivity, UserSex}

case class CreateUserCommand(email: String,
                             password: String,
                             sex: Option[String],
                             age: Option[Int],
                             height: Option[Int],
                             weight: Option[Int],
                             howActive: Option[String]) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  require(password != null, "Password cannot be null")
  require(password.trim.nonEmpty, "Password cannot be empty")
  private val sexValues: Set[String] = UserSex.values.map(v => v.toString)
  require(sex.isEmpty || sexValues.contains(sex.get), s"Sex can be one of: $sexValues")
  require(age.isEmpty || (age.get > 0 && age.get < 150), "Age has to be within a [0, 150] range")
  require(height.isEmpty || (height.get > 0 && height.get < 300), "Height has to be within a [0, 300] range")
  require(weight.isEmpty || (weight.get > 0 && weight.get < 500), "Weight has to be within a [0, 500] range")
  private val howActiveValues: Set[String] = UserActivity.values.map(v => v.toString)
  require(howActive.isEmpty || howActiveValues.contains(howActive.get), s"How active can be one of: $howActiveValues")
  def toUser: User = User(0, email, password, sex.map(UserSex.withName), age, height, weight, howActive.map(UserActivity.withName))
}
