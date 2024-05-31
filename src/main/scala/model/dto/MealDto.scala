package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class MealDto(@Schema(name = "meal_id", example = "1")
                   mealId: Long,
                   @Schema(example = "breakfast")
                   mealType: String,
                   @Schema(example = "500.0")
                   calories: Double,
                   @Schema(example = "20.0")
                   proteins: Double,
                   @Schema(example = "50.0")
                   carbohydrates: Double,
                   @Schema(example = "10.0")
                   fat: Double,
                   @Schema(example = "05/12/2024")
                   date: String
                  )
