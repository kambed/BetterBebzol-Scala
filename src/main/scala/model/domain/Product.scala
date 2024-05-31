package model.domain

import model.dto.ProductDto

case class Product(productId: Long,
                   productName: String,
                   calories: Option[Double],
                   proteins: Option[Double],
                   carbohydrates: Option[Double],
                   fat: Option[Double]) {

  def toProductDto: ProductDto = ProductDto(productId, productName, calories, proteins, carbohydrates, fat)
}