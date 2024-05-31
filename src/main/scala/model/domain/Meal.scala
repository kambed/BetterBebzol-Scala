package model.domain

import model.domain.enums.MealType.MealType
import model.dto.MealDto

case class Meal(mealId: Long,
                userId: Long,
                mealType: MealType,
                calories: Double = 0,
                proteins: Double = 0,
                carbohydrates: Double = 0,
                fat: Double = 0,
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