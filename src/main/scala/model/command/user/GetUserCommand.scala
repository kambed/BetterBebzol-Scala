package model.command.user

import model.command.abstracts.BaseCommand

case class GetUserCommand(email: String) extends BaseCommand