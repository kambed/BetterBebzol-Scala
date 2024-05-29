package model.command

import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.Meal
import model.domain.enums.MealType

case class CreateMealCommand(@Schema(example = "breakfast", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             mealType: Option[String],
                             @Schema(example = "01/01/2021", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                             date: Option[String],
                             userId: Long) extends BaseCommand {

  private val mealTypeValues: Set[String] = MealType.values.map(v => v.toString)
  require(mealType.isEmpty || mealTypeValues.contains(mealType.get), s"Meal type can be one of: $mealTypeValues")
  require(date.isEmpty || date.get.matches("^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/[0-9]{4}$"), "Date has to be in dd/MM/yyyy format")

  def toMeal: Meal = Meal(0, userId, MealType.withName(mealType.get), 0, 0, 0, 0, date.get)
}
