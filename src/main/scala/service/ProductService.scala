package service

import database.repository.ProductRepositoryComponent
import model.dto.ProductDto

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ProductServiceComponent { this: ProductRepositoryComponent =>
  def productService: ProductService

  class ProductService {

    def listAllProducts: Future[List[ProductDto]] = {
      productRepository.getAllProducts.map(
        productList => productList.map(
          product => ProductDto(product.id, product.name, product.calories)
        ).toList
      )
    }
  }
}
