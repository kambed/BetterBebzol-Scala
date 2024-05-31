package model.command.product

import model.command.abstracts.BaseCommand

case class GetMealProductsByIdCommand(mealId: Long, userId: Long) extends BaseCommand

