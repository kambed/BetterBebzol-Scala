package model.command.meal

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import model.domain.Meal
import model.domain.enums.MealType

case class EditMealCommand(
                            @Schema(name = "meal_type", example = "breakfast", requiredMode = Schema.RequiredMode.REQUIRED)
                            override val mealType: String,
                            @Schema(example = "01/01/2021", requiredMode = Schema.RequiredMode.REQUIRED)
                            override val date: Option[String],
                            @Hidden
                            override val userId: Long,
                            @Hidden
                            mealId: Long
                          ) extends CreateMealCommand(mealType, date, userId) {

  override def toMeal: Meal = {
    Meal(mealId, userId, MealType.withName(mealType), 0, 0, 0, 0, date.orNull)
  }
}

object EditMealCommand {
  def apply(
             mealType: String,
             date: Option[String],
             userId: Long,
             mealId: Long
           ): EditMealCommand = new EditMealCommand(mealType, date, userId, mealId)

  def unapply(cmd: EditMealCommand): Option[(String, Option[String], Long, Long)] =
    Some((cmd.mealType, cmd.date, cmd.userId, cmd.mealId))
}