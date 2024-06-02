package model.command.meal

import model.command.abstracts.BaseCommand

case class GetMealsByDateCommand(userId: Long, date: String) extends BaseCommand {
  require(date != null, "Date cannot be null")
  require(date.trim.nonEmpty, "Date cannot be empty")
  require(date.matches("\\d{2}/\\d{2}/\\d{4}"), "Invalid date format")
}
