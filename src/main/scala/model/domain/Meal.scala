package model.domain

import model.domain.MealTime.MealType
import model.dto.MealDto

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class Meal(mealId: Long,
                userId: Long,
                mealTime: MealType,
                calories: Float = 0,
                proteins: Float = 0,
                carbohydrates: Float = 0,
                fat: Float = 0,
                date: String = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
               ) {
  def toMealDto: MealDto = MealDto(mealId,
    mealTime.toString,
    calories,
    proteins,
    carbohydrates,
    fat,
    date)
}