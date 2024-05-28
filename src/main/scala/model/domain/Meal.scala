package model.domain

import model.domain.MealType.MealType
import model.dto.MealDto

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Meal(mealId: Long,
                userId: Long,
                mealType: MealType,
                calories: Float = 0,
                proteins: Float = 0,
                carbohydrates: Float = 0,
                fat: Float = 0,
                date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
               ) {
  def toMealDto: MealDto = MealDto(mealId,
    mealType.toString,
    calories,
    proteins,
    carbohydrates,
    fat,
    date)
}