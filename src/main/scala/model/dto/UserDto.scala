package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class UserDto(@Schema(example = "example@wp.pl")
                   email: String,
                   @Schema(example = "male")
                   sex: Option[String],
                   @Schema(example = "25")
                   age: Option[Int],
                   @Schema(example = "180")
                   height: Option[Int],
                   @Schema(example = "80")
                   weight: Option[Int],
                   @Schema(name = "how_active", example = "active")
                   howActive: Option[String],
                   @Schema(example = "lose_weight")
                   goal: Option[String])
