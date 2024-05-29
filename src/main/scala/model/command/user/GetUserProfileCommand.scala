package model.command

import model.command.abstracts.BaseCommand

case class GetUserProfileCommand(email: String) extends BaseCommand