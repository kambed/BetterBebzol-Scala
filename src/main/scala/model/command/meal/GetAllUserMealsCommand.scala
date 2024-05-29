package model.command.meal

import model.command.abstracts.BaseCommand

case class GetAllUserMealsCommand(userId: Long) extends BaseCommand
