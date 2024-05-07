package rest.api.controller

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import database.repository.ProductRepository
import service.ProductService
import spray.json.*

object ListAllProductsController extends BaseController {
  def apply(): Route = get {
    complete(HttpResponse(
      status = StatusCodes.OK,
      entity = HttpEntity(
        contentType = ContentTypes.`application/json`,
        string = ProductService(ProductRepository()).listAllProducts.toJson.toString
      )))
  }
}
