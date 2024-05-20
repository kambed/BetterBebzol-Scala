package model.domain

import model.dto.ProductDto

case class Product(productId: Long, userId: Long, productName: String, calories: Int, protein: Option[Double], fat: Option[Double], carbohydrates: Option[Double]) {
  def toProductDto: ProductDto = ProductDto(productId, userId, productName, calories, protein, fat, carbohydrates)
}