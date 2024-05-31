package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class MealProductDto(
                           @Schema(description = "Meal")
                           meal: MealDto,
                           @Schema(description = "List of products")
                           products: Seq[ProductQuantityDto]
                         )
