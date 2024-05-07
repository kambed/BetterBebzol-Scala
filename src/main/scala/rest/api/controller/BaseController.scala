package rest.api.controller

import akka.http.scaladsl.server.Directives
import model.json.JsonSupport

class BaseController extends Directives with JsonSupport