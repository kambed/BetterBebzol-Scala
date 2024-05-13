package rest.api.controller

import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout

import scala.concurrent.duration._
import model.json.JsonSupport

class BaseController extends Directives with JsonSupport {
  
  implicit val timeout: Timeout = Timeout(5.seconds)

  def completeNegative(value: Any): Route = {
    value match {
      case e: Exception => complete((500, Map("error" -> s"${e.getClass.getName}: ${e.getMessage}")))
      case _ => complete((500, Map("error" -> "unknown internal server error")))
    }
  }
}