package model.domain

import model.domain.enums.MealType.MealType
import model.dto.MealDto

case class Meal(mealId: Long,
                userId: Long,
                mealType: MealType,
                calories: Float = 0,
                proteins: Float = 0,
                carbohydrates: Float = 0,
                fat: Float = 0,
                date: String
               ) {
  def toMealDto: MealDto = MealDto(mealId,
    mealType.toString,
    calories,
    proteins,
    carbohydrates,
    fat,
    date)
}