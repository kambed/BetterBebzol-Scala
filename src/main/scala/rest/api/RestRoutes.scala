package rest.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpResponse}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, Route}
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import model.command.abstracts.Command
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode401, ExceptionWithResponseCode403, ExceptionWithResponseCode404}
import rest.api.controller.login.{EditUserPasswordController, LoginUserController}
import rest.api.controller.meal.CreateMealController
import rest.api.controller.product.{CreateProductController, ListAllProductsController}
import rest.api.controller.user._
import util.swagger.SwaggerDocService

class RestRoutes(implicit system: ActorSystem[Command]) {

  private val corsSettings: CorsSettings = CorsSettings.defaultSettings.withAllowedMethods(Seq(HttpMethods.GET,
    HttpMethods.POST, HttpMethods.PUT, HttpMethods.DELETE, HttpMethods.HEAD, HttpMethods.OPTIONS))
  private val handleErrors = handleRejections(rejectionHandler) & handleExceptions(exceptionHandler)
  val allRoutes: Route = cors(corsSettings) {
    handleErrors {
      pathPrefix("api") {
        pathPrefix("v1") {
          productRoutes ~
            userRoutes ~
            mealRoutes
        }
      } ~ SwaggerDocService.routes
    }
  }

  private lazy val userRoutes: Route = pathPrefix("user") {
    pathEnd {
      CreateUserController(system) ~
        EditUserController(system) ~
        GetLoggedUserController(system)
    } ~ path("login") {
      LoginUserController(system) ~
        EditUserPasswordController(system)
    } ~ path("recommendations") {
      GetUserRecommendationsController(system)
    } ~ path(Segment) { email =>
      GetUserController(system, email)
    }
  }

  private lazy val mealRoutes: Route = path("meal") {
    //    pathEnd {
    //      CreateMealController(system)
    //    }
    CreateMealController(system)
  }

  private lazy val productRoutes: Route = path("product") {
    ListAllProductsController(system) ~
      CreateProductController(system)
  }

  private def rejectionHandler: RejectionHandler =
    RejectionHandler.default
      .mapRejectionResponse {
        case res@HttpResponse(_, _, ent: HttpEntity.Strict, _) =>
          val message = ent.data.utf8String.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
          res.withEntity(HttpEntity(ContentTypes.`application/json`, s"""{"rejection": "$message"}"""))
        case x => x
      }

  private def exceptionHandler: ExceptionHandler =
    ExceptionHandler {
      case e400: ExceptionWithResponseCode400 =>
        val message = e400.getMessage.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
        complete(HttpResponse(400, entity = HttpEntity(ContentTypes.`application/json`, s"""{"Bad request": "$message"}""")))
      case e401: ExceptionWithResponseCode401 =>
        val message = e401.getMessage.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
        complete(HttpResponse(401, entity = HttpEntity(ContentTypes.`application/json`, s"""{"Unauthorized": "$message"}""")))
      case e403: ExceptionWithResponseCode403 =>
        val message = e403.getMessage.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
        complete(HttpResponse(403, entity = HttpEntity(ContentTypes.`application/json`, s"""{"Forbidden": "$message"}""")))
      case e404: ExceptionWithResponseCode404 =>
        val message = e404.getMessage.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
        complete(HttpResponse(404, entity = HttpEntity(ContentTypes.`application/json`, s"""{"Not found": "$message"}""")))
      case e: Exception =>
        extractUri { uri =>
          system.log.error(s"Request to $uri could not be handled normally", e)
          val message = e.getMessage.replaceAll("\"", "\'").replaceAll("\n", " ").replaceAll(" {2}", " ")
          complete(HttpResponse(500, entity = HttpEntity(ContentTypes.`application/json`, s"""{"Internal server error": "$message"}""")))
        }
    }
}
