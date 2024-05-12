package rest.api.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import database.repository.ProductRepositoryComponent
import service.ProductServiceComponent

object ListAllProductsController extends BaseController with ProductServiceComponent with ProductRepositoryComponent {
  val productRepository: ListAllProductsController.ProductRepository = new ProductRepository
  val productService: ListAllProductsController.ProductService = new ProductService

  def apply(): Route = get {
    complete(StatusCodes.OK, productService.listAllProducts)
  }
}
