package rest.api.controller.meal

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{POST, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.meal.CreateMealCommand
import model.domain.Meal
import model.dto.MealDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

import scala.concurrent.Future

object CreateMealController {
  def apply(implicit system: ActorSystem[_]): Route = new CreateMealController().route()
}

@Path("/api/v1/meal")
class CreateMealController(implicit system: ActorSystem[_]) extends BaseAuthenticatedController {

  @POST
  @Operation(summary = "Create meal", tags = Array("meal"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[CreateMealCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "201", content = Array(new Content(schema = new Schema(implementation = classOf[MealDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = post {
    authenticatedRoute { userId =>
      entity(as[CreateMealCommand]) { createMealCommand =>
        val result: Future[Command] = Actors.getActorRef(ActorType.MEAL_DATABASE)
          .ask(ref => Command(CreateMealCommand(createMealCommand.mealType, createMealCommand.date, userId),
            ref))
        onSuccess(result) { result: Command =>
          result.command match {
            case returnCommand: ReturnCommand => returnCommand.response match {
              case meal: Meal => complete(StatusCodes.Created, meal.toMealDto)
              case other => completeNegative(other)
            }
            case other => completeNegative(other)
          }
        }
      }
    }
  }
}