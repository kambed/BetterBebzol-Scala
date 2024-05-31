package rest.api.controller

import akka.http.scaladsl.server.Route
import util.jwt.TokenAuthorization

abstract class BaseAuthenticatedController extends BaseController {

  def authenticatedRoute(innerRoute: Long => Route): Route = {
    TokenAuthorization.authenticated { claim =>
      val userId: Option[Any] = claim.get("id")
      if (userId.isEmpty) {
        completeWith401()
      } else {
        innerRoute(retrieveUserId(userId))
      }
    }
  }

  protected def retrieveUserId(userId: Option[Any]): Long = {
    val javaInteger: String = userId.get.toString
    javaInteger.toLong
  }
}