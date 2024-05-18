package model.command

import model.command.abstracts.BaseCommand
import model.domain.{User, UserActivity}

case class CreateUserCommand(email: String,
                             password: String,
                             age: Option[Int],
                             height: Option[Double],
                             weight: Option[Double],
                             howActive: Option[String]) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  require(password != null, "Password cannot be null")
  require(password.trim.nonEmpty, "Password cannot be empty")
  require(age.isEmpty || (age.get > 0 && age.get < 150), "Age has to be within a [0, 150] range")
  require(height.isEmpty || (height.get > 0 && height.get < 300), "Height has to be within a [0, 300] range")
  require(weight.isEmpty || (weight.get > 0 && weight.get < 500), "Weight has to be within a [0, 500] range")
  private val howActiveValues: Set[String] = UserActivity.values.map(v => v.toString)
  require(howActive.isEmpty || howActiveValues.contains(howActive.get), s"How active can be one of: $howActiveValues")
  private val howActiveEnum = if (howActive.isEmpty) UserActivity.sedentary else UserActivity.withName(howActive.get)
  def toUser: User = User(0, email, password, age, height, weight, howActiveEnum)
}
