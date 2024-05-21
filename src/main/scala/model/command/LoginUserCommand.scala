package model.command

import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand

case class LoginUserCommand(@Schema(example = "example@wp.pl", requiredMode = Schema.RequiredMode.REQUIRED)
                            email: String,
                            @Schema(example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
                            password: String) extends BaseCommand {
  require(email != null, "Email cannot be null")
  require(email.trim.nonEmpty, "Email cannot be empty")
  require(password != null, "Password cannot be null")
  require(password.trim.nonEmpty, "Password cannot be empty")
}
