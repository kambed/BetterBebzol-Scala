package model.command

import model.domain.Product

case class CreateProductCommand(name: String, calories: Integer) {
  require(name != null, "Name cannot be null")
  require(name.trim.nonEmpty, "Name cannot be empty")
  require(calories != null, "Calories cannot be null")
  require(calories >= 0, "Calories must be greater than or equal to 0")

  def toProduct: Product = Product(0, name, calories)
}
