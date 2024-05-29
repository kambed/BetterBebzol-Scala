package model.command.product

import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.Product

case class CreateProductCommand(@Schema(name = "product_name", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
                                productName: String,
                                @Schema(example = "52", requiredMode = Schema.RequiredMode.REQUIRED)
                                calories: Int,
                                @Schema(example = "0.3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                protein: Option[Double],
                                @Schema(example = "0.2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                fat: Option[Double],
                                @Schema(example = "14.0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
                                carbohydrates: Option[Double]) extends BaseCommand {
  require(productName != null, "Name cannot be null")
  require(productName.trim.nonEmpty, "Name cannot be empty")
  require(calories >= 0, "Calories must be greater than or equal to 0")
  require(protein.isEmpty || protein.get >= 0, "Protein must be greater than or equal to 0")
  require(fat.isEmpty || fat.get >= 0, "Fat must be greater than or equal to 0")
  require(carbohydrates.isEmpty || carbohydrates.get >= 0, "Carbohydrates must be greater than or equal to 0")

  //TODO: 1 should be replaced with user id from token
  def toProduct: Product = Product(0, 1, productName, calories, protein, fat, carbohydrates)
}
