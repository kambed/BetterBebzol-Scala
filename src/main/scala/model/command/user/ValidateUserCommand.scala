package model.command.user

import model.command.abstracts.BaseCommand
import model.domain.User

case class ValidateUserCommand(givenPassword: String, user: User) extends BaseCommand
