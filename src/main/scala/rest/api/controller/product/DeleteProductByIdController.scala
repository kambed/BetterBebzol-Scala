package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{DELETE, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.product.DeleteProductByIdCommand
import model.domain.Product
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}


object DeleteProductByIdController {
  def apply(implicit system: ActorSystem[_], productId: Long): Route = new DeleteProductByIdController().route()

}

@Path("/api/v1/product/{productId}")
class DeleteProductByIdController(implicit system: ActorSystem[_], productId: Long) extends BaseAuthenticatedController {

  @DELETE
  @Operation(summary = "Delete a product by id", tags = Array("product"),
    parameters = Array(
      new Parameter(name = "productId", in = ParameterIn.PATH, required = true, description = "Product id", content = Array(new Content(schema = new Schema(implementation = classOf[Long]))))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Product deleted"),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = delete {
    authenticatedRoute { userId =>
      val result = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
        .ask(ref => Command(DeleteProductByIdCommand(productId, userId), ref))
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