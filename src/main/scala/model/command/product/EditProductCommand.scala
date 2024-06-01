package model.command.product

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.media.Schema
import model.command.abstracts.BaseCommand
import model.domain.Product

case class EditProductCommand(@Schema(name = "product_name", example = "Apple", requiredMode = Schema.RequiredMode.REQUIRED)
                              productName: String,
                              @Schema(example = "52.5", requiredMode = Schema.RequiredMode.REQUIRED)
                              calories: Option[Double],
                              @Schema(example = "0.3", requiredMode = Schema.RequiredMode.REQUIRED)
                              protein: Option[Double],
                              @Schema(example = "0.2", requiredMode = Schema.RequiredMode.REQUIRED)
                              fat: Option[Double],
                              @Schema(example = "14.1", requiredMode = Schema.RequiredMode.REQUIRED)
                              carbohydrates: Option[Double],
                              @Schema(example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
                              quantity: Option[Int],
                              @Hidden
                              userId: Long,
                              @Hidden
                              productId: Long) extends BaseCommand {

  require(productName != null, "Name cannot be null")
  require(productName.trim.nonEmpty, "Name cannot be empty")
  require(calories.isDefined || calories.get >= 0, "Calories must be greater than or equal to 0")
  require(protein.isEmpty || protein.get >= 0, "Protein must be greater than or equal to 0")
  require(fat.isEmpty || fat.get >= 0, "Fat must be greater than or equal to 0")
  require(carbohydrates.isEmpty || carbohydrates.get >= 0, "Carbohydrates must be greater than or equal to 0")
  require(quantity.isDefined || quantity.get > 0, "Quantity must be greater than 0")

  def toProduct: Product = Product(productId, productName, calories, protein, fat, carbohydrates)
}

