package service

import database.repository.ProductRepositoryComponent
import model.command.CreateProductCommand
import model.dto.ProductDto

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

trait ProductServiceComponent { this: ProductRepositoryComponent =>
  def productService: ProductService

  class ProductService {

    def listAllProducts: Future[List[ProductDto]] = {
      productRepository.getAllProducts.map(
        productList => productList.map(_.toProductDto).toList
      )
    }
    
    def createProduct(createProductCommand: CreateProductCommand): Future[ProductDto] = {
      productRepository.insertProduct(createProductCommand.toProduct).map(_.toProductDto)
    }
  }
}
