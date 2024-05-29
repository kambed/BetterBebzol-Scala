package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class UserProfileDto(@Schema(example = "example@wp.pl")
                          email: String,
                          @Schema(example = "maintain_weight")
                          goal: String,
                          @Schema(example = "2401")
                          calories: Int,
                          @Schema(example = "81.6")
                          protein: Double,
                          @Schema(example = "68")
                          fat: Double,
                          @Schema(example = "330.14")
                          carbohydrates: Double)
