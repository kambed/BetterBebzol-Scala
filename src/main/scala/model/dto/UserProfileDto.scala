package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class UserProfileDto(@Schema(example = "example@wp.pl")
                          email: String,
                          @Schema(example = "2000")
                          recommendedCalories: Int,
                          @Schema(example = "100.0")
                          recommendedProtein: Double,
                          @Schema(example = "100.0")
                          recommendedFat: Double,
                          @Schema(example = "100.0")
                          recommendedCarbohydrates: Double)
