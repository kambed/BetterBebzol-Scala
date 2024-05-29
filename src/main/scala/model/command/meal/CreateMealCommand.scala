package model.command.meal

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.Meal
import model.domain.enums.MealType

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class CreateMealCommand(@Schema(example = "breakfast", requiredMode = Schema.RequiredMode.REQUIRED)
                             mealType: Option[String],
                             @Schema(example = "01/01/2021", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             date: Option[String],
                             @Hidden
                             userId: Long) extends BaseCommand {

  private val mealTypeValues: Set[String] = MealType.values.map(v => v.toString)
  require(mealType.isEmpty || mealTypeValues.contains(mealType.get), s"Meal type can be one of: $mealTypeValues")
  private val optionalDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

  def toMeal: Meal = Meal(0, userId, MealType.withName(mealType.get), 0, 0, 0, 0, date.getOrElse(optionalDate))
}
