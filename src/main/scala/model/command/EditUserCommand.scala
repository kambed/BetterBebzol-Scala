package model.command

import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.User
import model.domain.enums.{UserActivity, UserSex}

case class EditUserCommand(@Schema(example = "example@wp.pl", requiredMode = Schema.RequiredMode.REQUIRED)
                             email: String,
                           @Schema(example = "male", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = Array("male", "female"))
                             sex: Option[String],
                           @Schema(example = "25", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             age: Option[Int],
                           @Schema(example = "180", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             height: Option[Int],
                           @Schema(example = "80", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             weight: Option[Int],
                           @Schema(name = "how_active", example = "active", requiredMode = Schema.RequiredMode.NOT_REQUIRED, allowableValues = Array("sedentary", "little", "moderate", "active", "very_active"))
                             howActive: Option[String]) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  private val sexValues: Set[String] = UserSex.values.map(v => v.toString)
  require(sex.isEmpty || sexValues.contains(sex.get), s"Sex can be one of: $sexValues")
  require(age.isEmpty || (age.get > 0 && age.get < 150), "Age has to be within a [0, 150] range")
  require(height.isEmpty || (height.get > 0 && height.get < 300), "Height has to be within a [0, 300] range")
  require(weight.isEmpty || (weight.get > 0 && weight.get < 500), "Weight has to be within a [0, 500] range")
  private val howActiveValues: Set[String] = UserActivity.values.map(v => v.toString)
  require(howActive.isEmpty || howActiveValues.contains(howActive.get), s"How active can be one of: $howActiveValues")

  def toUser: User = User(0, email, null, sex.map(UserSex.withName), age, height, weight, howActive.map(UserActivity.withName))
}
