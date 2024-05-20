package rest.api.controller

import akka.http.scaladsl.server.{Directives, Route, StandardRoute}
import akka.util.Timeout
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode404}
import util.json.JsonSupport

import scala.concurrent.duration._

class BaseController extends Directives with JsonSupport {

  implicit val timeout: Timeout = Timeout(5.seconds)

  def completeNegative(value: Any): Route = {
    value match {
      case e400: ExceptionWithResponseCode400 => completeWith400(e400.getMessage)
      case e404: ExceptionWithResponseCode404 => completeWith404(e404.getMessage)
      case e: Exception => completeWith500(s"${e.getClass.getName}: ${e.getMessage}")
      case _ => completeWith500()
    }
  }

  protected def completeWith400(error: String): Route = {
    complete(400, Map("error" -> error))
  }

  protected def completeWith404(error: String): Route = {
    complete(404, Map("error" -> error))
  }

  protected def completeWith500(error: String): Route = {
    complete(500, Map("error" -> error))
  }

  protected def completeWith500(): Route = {
    completeWith500("Unknown internal server error")
  }

  protected def completeWith401(): StandardRoute = {
    complete(401, Map("error" -> "Unauthorized"))
  }

  protected def completeWith403(): StandardRoute = {
    complete(403, Map("error" -> "Forbidden"))
  }
}