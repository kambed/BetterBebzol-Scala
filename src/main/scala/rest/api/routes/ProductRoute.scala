package rest.api.routes

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import rest.api.controller.{CreateProductController, ListAllProductsController}

class ProductRoute {

  val route: Route = {
    path("product") {
      ListAllProductsController() ~
        CreateProductController()
    }
  }
}
