package model.command.meal

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema

case class EditMealCommand(
                            @Schema(example = "breakfast", requiredMode = Schema.RequiredMode.REQUIRED)
                            override val mealType: Option[String],
                            @Schema(example = "01/01/2021", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                            override val date: Option[String],
                            @Hidden
                            override val userId: Long,
                            @Hidden
                            mealId: Long
                          ) extends CreateMealCommand(mealType, date, userId)

object EditMealCommand {
  def apply(
             mealType: Option[String],
             date: Option[String],
             userId: Long,
             mealId: Long
           ): EditMealCommand = new EditMealCommand(mealType, date, userId, mealId)

  def unapply(cmd: EditMealCommand): Option[(Option[String], Option[String], Long, Long)] =
    Some((cmd.mealType, cmd.date, cmd.userId, cmd.mealId))
}