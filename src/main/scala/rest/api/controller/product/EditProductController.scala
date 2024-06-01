package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{PUT, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.product.EditProductCommand
import model.domain.Product
import model.dto.ProductDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

import scala.concurrent.Future

object EditProductController {
  def apply(implicit system: ActorSystem[_], productId: Long): Route = new EditProductController().route()

}

@Path("/api/v1/product/{productId}")
class EditProductController(implicit system: ActorSystem[_], productId: Long) extends BaseAuthenticatedController {

  @PUT
  @Operation(summary = "Edit a product", tags = Array("product"),
    parameters = Array(
      new Parameter(name = "productId", in = ParameterIn.PATH, required = true, description = "Product id", content = Array(new Content(schema = new Schema(implementation = classOf[Long]))))),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[EditProductCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[ProductDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = put {
    authenticatedRoute { userId =>
      entity(as[EditProductCommand]) { editProductCommand =>
        val result: Future[Command] = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
          .ask(ref => Command(EditProductCommand(editProductCommand.productName, editProductCommand.calories,
            editProductCommand.protein, editProductCommand.fat, editProductCommand.carbohydrates, editProductCommand.quantity, userId, productId),
            ref))
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
}