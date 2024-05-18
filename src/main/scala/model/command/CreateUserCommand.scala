package model.command

import model.command.abstracts.BaseCommand
import model.domain.{User, UserActivity}

case class CreateUserCommand(email: String,
                             password: String,
                             age: Int,
                             height: Double,
                             weight: Double,
                             howActive: String) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  require(password != null, "Password cannot be null")
  require(password.trim.nonEmpty, "Password cannot be empty")
  require(age == null || (age > 0 && age < 150), "Age has to be within a [0, 150] range")
  require(height == null || (height > 0 && height < 300), "Height has to be within a [0, 300] range")
  require(weight == null || (weight > 0 && weight < 500), "Weight has to be within a [0, 500] range")
  private val howActiveValues: Set[String] = UserActivity.values.map(v => v.toString)
  require(howActive == null || howActiveValues.contains(howActive), s"How active can be one of: $howActiveValues")

  def toUser: User = User(0, email, password, age, height, weight, UserActivity.withName(howActive))
}
