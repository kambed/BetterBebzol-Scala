package model.command.meal

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.Hidden
import model.command.abstracts.BaseCommand
import model.domain.{Meal, MealType}

class CreateMealCommand(
                         @Schema(example = "breakfast", requiredMode = Schema.RequiredMode.REQUIRED)
                         val mealType: Option[String],
                         @Schema(example = "01/01/2021", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                         val date: Option[String],
                         @Hidden
                         val userId: Long
                       ) extends BaseCommand {

  private val mealTypeValues: Set[String] = MealType.values.map(_.toString)
  require(mealType.isEmpty || mealTypeValues.contains(mealType.get), s"Meal type can be one of: $mealTypeValues")
  private val optionalDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

  def toMeal: Meal = Meal(0, userId, MealType.withName(mealType.get), 0, 0, 0, 0, date.getOrElse(optionalDate))
}

object CreateMealCommand {
  def apply(
             mealType: Option[String],
             date: Option[String],
             userId: Long
           ): CreateMealCommand = new CreateMealCommand(mealType, date, userId)

  def unapply(cmd: CreateMealCommand): Option[(Option[String], Option[String], Long)] =
    Some((cmd.mealType, cmd.date, cmd.userId))
}