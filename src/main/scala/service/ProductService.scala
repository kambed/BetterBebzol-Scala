package service

import database.repository.ProductRepository
import model.dto.ProductDto

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class ProductService(val productRepository: ProductRepository) {

  def listAllProducts: Future[List[ProductDto]] = {
    productRepository.getAllProducts.map(
      productList => productList.map(
        product => ProductDto(product.id, product.name, product.calories)
      ).toList
    )
  }
}
