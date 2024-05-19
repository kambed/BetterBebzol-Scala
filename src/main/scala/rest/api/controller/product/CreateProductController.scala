package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{POST, Path}
import model.command.CreateProductCommand
import model.command.abstracts.Command
import model.domain.Product
import model.dto.ProductDto
import rest.api.controller.BaseController
import util.{ActorType, Actors}

object CreateProductController {
  def apply(implicit system: ActorSystem[_]): Route = new CreateProductController().route()
}

@Path("/api/v1/product")
class CreateProductController(implicit system: ActorSystem[_]) extends BaseController {

  @POST
  @Operation(summary = "Create a product", tags = Array("product"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[CreateProductCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "201", content = Array(new Content(schema = new Schema(implementation = classOf[ProductDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = post {
    entity(as[CreateProductCommand]) { createProductCommand =>
      val actorRef = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
      val result = actorRef.ask(ref => Command(createProductCommand, ref))
      onSuccess(result) {
        case product: Product => complete(StatusCodes.Created, product.toProductDto)
        case other => completeNegative(other)
      }
    }
  }
}
