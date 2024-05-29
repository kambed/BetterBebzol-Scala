package model.command.meal

import model.command.abstracts.BaseCommand

case class GetMealsByDateCommand(userId: Long, date: String) extends BaseCommand
