package rest.api

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import rest.api.controller.{CreateProductController, ListAllProductsController}

class RestRoutes {

  val allRoutes: Route =
    pathPrefix("api") {
      pathPrefix("v1") {
        productRoutes
      } ~ pathPrefix("v1") {
        userRoutes
      } ~ path("healthcheck") {
        get {
          complete("OK")
        }
      }
    }

  private lazy val productRoutes: Route = path("product") {
    ListAllProductsController() ~
      CreateProductController()
  }

  private lazy val userRoutes: Route = path("user") {
    get {
      complete("user")
    }
  }
}
