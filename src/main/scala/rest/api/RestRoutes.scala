package rest.api

import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import rest.api.controller.{CreateProductController, ListAllProductsController}
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse, StatusCodes}
import akka.http.scaladsl.server.{Directives, RejectionHandler}

class RestRoutes {

  val allRoutes: Route = Route.seal(
    pathPrefix("api") {
      pathPrefix("v1") {
        productRoutes
      } ~ path("healthcheck") {
        get {
          complete("OK")
        }
      }
    }
  )

  private lazy val productRoutes: Route = path("product") {
    ListAllProductsController() ~
      CreateProductController()
  }

  implicit def rejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case res@HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          val message = ent.data.utf8String.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
          res.withEntity(HttpEntity(ContentTypes.`application/json`, s"""{"rejection": "$message"}"""))
        case x => x
      }
}
