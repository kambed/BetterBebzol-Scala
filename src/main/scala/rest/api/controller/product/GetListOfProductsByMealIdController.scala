package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.product.GetMealProductsByIdCommand
import model.dto.MealProductDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

object GetListOfProductsByMealIdController {
  def apply(implicit system: ActorSystem[_]): Route = new GetListOfProductsByMealIdController().route()
}

@Path("/api/v1/product/all")
class GetListOfProductsByMealIdController(implicit system: ActorSystem[_]) extends BaseAuthenticatedController {

  @GET
  @Path("/{mealId}")
  @Operation(summary = "Get list of products by meal id", tags = Array("product"),
    parameters = Array(
      new Parameter(name = "mealId", in = io.swagger.v3.oas.annotations.enums.ParameterIn.PATH, required = true, description = "Meal id", content = Array(new Content(schema = new Schema(implementation = classOf[Long]))))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[MealProductDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    parameters("mealId".as[Long]) { mealId =>
      authenticatedRoute { userId =>
        val result = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
          .ask(ref => Command(GetMealProductsByIdCommand(mealId, userId), ref))
        onSuccess(result) { result: Command =>
          result.command match {
            case returnCommand: ReturnCommand => returnCommand.response match {
              case mealProduct: MealProductDto => complete(StatusCodes.OK, mealProduct)
              case other => completeNegative(other)
            }
            case other => completeNegative(other)
          }
        }
      }
    }
  }
}