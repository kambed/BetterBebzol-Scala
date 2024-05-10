package model.command

import model.domain.Product

case class CreateProductCommand(name: String, calories: Int) {
  def toProduct: Product = Product(0, name, calories)
}
