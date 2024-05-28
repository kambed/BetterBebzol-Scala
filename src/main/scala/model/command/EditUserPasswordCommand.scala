package model.command

import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.User

case class EditUserPasswordCommand(@Schema(example = "example@wp.pl", requiredMode = Schema.RequiredMode.REQUIRED)
                                   email: String,
                                   @Schema(example = "password2", requiredMode = Schema.RequiredMode.REQUIRED)
                                   password: String) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  require(password != null, "Password cannot be null")
  require(password.trim.nonEmpty, "Password cannot be empty")

  def toUser: User = User(0, email, password, None, None, None, None, None)
}
