package rest.api.controller

import akka.http.scaladsl.server.Route
import model.command.CreateProductCommand
import model.domain.Product

object CreateProductController extends BaseController {

  def apply(): Route = post {
    entity(as[CreateProductCommand]) { createProductCommand =>
      val product = Product(1, createProductCommand.name, createProductCommand.calories)
      complete(s"Product created: $product")
    }
  }
}
