package model.domain

import model.dto.ProductDto

case class Product(id: Long, name: String, calories: Int) {
  def toProductDto: ProductDto = ProductDto(id, name, calories)
}