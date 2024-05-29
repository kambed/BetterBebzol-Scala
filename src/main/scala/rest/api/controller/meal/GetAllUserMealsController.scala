package rest.api.controller.meal

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.meal.GetAllUserMealsCommand
import model.domain.Meal
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

object GetAllUserMealsController {
  def apply(implicit system: ActorSystem[_]): Route = new GetAllUserMealsController().route()
}

@Path("/api/v1/meal/all")
class GetAllUserMealsController(implicit system: ActorSystem[_]) extends BaseAuthenticatedController {

  @GET
  @Operation(summary = "Get all user meals", tags = Array("meal"),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[List[Meal]])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error")))
  def route(): Route = get {
    authenticatedRoute { userId =>
      val result = Actors.getActorRef(ActorType.MEAL_DATABASE).ask(ref => Command(GetAllUserMealsCommand(userId), ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case meals: Seq[Meal] => complete(StatusCodes.OK, meals.map(_.toMealDto))
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}