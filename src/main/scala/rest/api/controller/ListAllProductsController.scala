package rest.api.controller

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import database.repository.ProductRepository
import service.ProductService

object ListAllProductsController extends BaseController {
  def apply(): Route = get {
    complete(StatusCodes.OK, ProductService(ProductRepository()).listAllProducts)
  }
}
