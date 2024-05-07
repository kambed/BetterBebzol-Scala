package rest.api.controller

import akka.http.scaladsl.server.Route

object ListAllProductsController extends BaseController {
  def apply(): Route = get {
    complete("Get all products")
  }
}
