package service

import database.repository.ProductRepository
import model.dto.ProductDto

import scala.concurrent.Await
import scala.concurrent.duration.*

class ProductService(val productRepository: ProductRepository) {

  def listAllProducts: Seq[ProductDto] = {
    Await.result(productRepository.getAllProducts, 1000.millis).map(product => ProductDto(product.id, product.name, product.calories))
  }
}
