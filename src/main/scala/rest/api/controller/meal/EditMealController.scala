package rest.api.controller.meal

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{PUT, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.meal.EditMealCommand
import model.domain.Meal
import model.dto.MealDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

import scala.concurrent.Future

object EditMealController {
  def apply(implicit system: ActorSystem[_], mealId: Long): Route = new EditMealController().route()
}

@Path("/api/v1/meal")
class EditMealController(implicit system: ActorSystem[_], mealId: Long) extends BaseAuthenticatedController {

  @PUT
  @Path("/{mealId}")
  @Operation(summary = "Edit meal", tags = Array("meal"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[EditMealCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "201", content = Array(new Content(schema = new Schema(implementation = classOf[MealDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = put {
    authenticatedRoute { userId =>
      entity(as[EditMealCommand]) { editMealCommand =>
        val result: Future[Command] = Actors.getActorRef(ActorType.MEAL_DATABASE)
          .ask(ref => Command(EditMealCommand(editMealCommand.mealType, editMealCommand.date, userId, mealId),
            ref))
        onSuccess(result) { result: Command =>
          result.command match {
            case returnCommand: ReturnCommand => returnCommand.response match {
              case meal: Meal => complete(StatusCodes.Accepted, meal.toMealDto)
              case other => completeNegative(other)
            }
            case other => completeNegative(other)
          }
        }
      }
    }
  }
}