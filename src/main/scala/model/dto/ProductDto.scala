package model.dto

case class ProductDto(productId: Long, userId: Long, productName: String, calories: Int, protein: Option[Double], fat: Option[Double], carbohydrates: Option[Double])
