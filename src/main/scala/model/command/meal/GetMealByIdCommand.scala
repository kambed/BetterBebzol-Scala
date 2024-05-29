package model.command.meal

import model.command.abstracts.BaseCommand

case class GetMealByIdCommand(userId: Long, mealId: Long) extends BaseCommand
