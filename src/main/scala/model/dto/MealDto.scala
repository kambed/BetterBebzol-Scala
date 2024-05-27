package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class MealDto(@Schema(name = "meal_id", example = "1")
                   mealId: Long,
                   @Schema(example = "breakfast")
                   mealTime: String,
                   @Schema(example = "500.0")
                   calories: Float ,
                   @Schema(example = "20.0")
                   proteins: Float,
                   @Schema(example = "50.0")
                   carbohydrates: Float,
                   @Schema(example = "10.0")
                   fats: Float,
                   @Schema(example = "05/12/2024")
                   date: String
                  )
