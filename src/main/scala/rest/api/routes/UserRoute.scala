package rest.api.routes

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route

class UserRoute {

  val route: Route = {
    path("user") {
      get {
        complete("user")
      }
    }
  }
}
