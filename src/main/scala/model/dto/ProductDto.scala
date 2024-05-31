package model.dto

import io.swagger.v3.oas.annotations.media.Schema

case class ProductDto(@Schema(example = "1")
                      productId: Long,
                      @Schema(example = "Apple")
                      productName: String,
                      @Schema(example = "52")
                      calories: Option[Double],
                      @Schema(example = "0.3")
                      protein: Option[Double],
                      @Schema(example = "0.2")
                      fat: Option[Double],
                      @Schema(example = "14.0")
                      carbohydrates: Option[Double]
                     )
