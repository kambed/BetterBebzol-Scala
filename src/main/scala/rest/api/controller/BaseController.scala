package rest.api.controller

import akka.http.scaladsl.server.Directives
import akka.util.Timeout
import scala.concurrent.duration._
import model.json.JsonSupport

class BaseController extends Directives with JsonSupport {
  
  implicit val timeout: Timeout = Timeout(5.seconds)
}