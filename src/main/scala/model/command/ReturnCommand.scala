package model.command

import model.command.abstracts.BaseCommand

case class ReturnCommand(response: Any) extends BaseCommand
