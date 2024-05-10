package rest.api.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import model.command.CreateProductCommand
import rest.api.controller.ListAllProductsController.{ProductRepository, ProductService}

object CreateProductController extends BaseController {
  val productRepository: ListAllProductsController.ProductRepository = new ProductRepository
  val productService: ListAllProductsController.ProductService = new ProductService

  def apply(): Route = post {
    entity(as[CreateProductCommand]) { createProductCommand =>
      complete(StatusCodes.OK, productService.createProduct(createProductCommand))
    }
  }
}
