package rest.api

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import rest.api.routes.{ProductRoute, UserRoute}

class RestRoutes {

  val allRoutes: Route =
    pathPrefix("api") {
      pathPrefix("v1") {
        ProductRoute().route
      } ~ pathPrefix("v1") {
        UserRoute().route
      } ~ path("healthcheck") {
        get {
          complete("OK")
        }
      }
    }
}
