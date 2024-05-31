package model.command.user

import model.command.abstracts.BaseCommand

case class GetUserProfileCommand(email: String) extends BaseCommand