package rest.api.controller.meal

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.{GET, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.meal.GetMealByIdCommand
import model.domain.Meal
import model.dto.MealDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

object GetMealByIdController {
  def apply(implicit system: ActorSystem[_], mealId: Long): Route = new GetMealByIdController().route()
}

@Path("/api/v1/meal/{mealId}")
class GetMealByIdController(implicit system: ActorSystem[_], mealId: Long) extends BaseAuthenticatedController {

  @GET
  @Operation(summary = "Get meal by id", tags = Array("meal"),
    parameters = Array(
      new Parameter(name = "mealId", in = ParameterIn.PATH, required = true, description = "Meal id", content = Array(new Content(schema = new Schema(implementation = classOf[Long]))))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[MealDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    authenticatedRoute { userId =>
      val result = Actors.getActorRef(ActorType.MEAL_DATABASE).ask(ref => Command(GetMealByIdCommand(userId, mealId), ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case meal: Meal => complete(StatusCodes.OK, meal.toMealDto)
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}