package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.product.GetProductByIdCommand
import model.domain.Product
import model.dto.ProductDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

object GetProductByIdController {
  def apply(implicit system: ActorSystem[_], productId: Long): Route = new GetProductByIdController().route()

}

@Path("/api/v1/product")
class GetProductByIdController(implicit system: ActorSystem[_], productId: Long) extends BaseAuthenticatedController {

  @GET
  @Path("/{productId}")
  @Operation(summary = "Get a product by id", tags = Array("product"),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[ProductDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    authenticatedRoute { userId =>
      val result = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
        .ask(ref => Command(GetProductByIdCommand(productId, userId), ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case product: Product => complete(StatusCodes.OK, product.toProductDto)
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}