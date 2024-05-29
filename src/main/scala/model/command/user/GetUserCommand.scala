package model.command

import model.command.abstracts.BaseCommand

case class GetUserCommand(email: String) extends BaseCommand