package rest.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpResponse}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{RejectionHandler, Route}
import model.command.abstracts.Command
import rest.api.controller.product.{CreateProductController, ListAllProductsController}
import rest.api.controller.user.{CreateUserController, GetUserController}
import util.swagger.SwaggerDocService

class RestRoutes(implicit system: ActorSystem[Command]) {

  val allRoutes: Route = Route.seal(
    cors()(pathPrefix("api") {
      pathPrefix("v1") {
        productRoutes ~
          userRoutes
      }
    } ~ SwaggerDocService.routes)
  )

  private lazy val userRoutes: Route = pathPrefix("user") {
    pathEnd {
      CreateUserController(system)
    } ~ path(Segment) { email =>
      GetUserController(system, email)
    }
  }

  private lazy val productRoutes: Route = path("product") {
    ListAllProductsController(system) ~
      CreateProductController(system)
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
