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
import model.command.exception.ExceptionWithResponseCode400
import model.command.meal.GetMealsByDateCommand
import model.domain.Meal
import model.dto.MealDto
import rest.api.controller.BaseAuthenticatedController
import util.{ActorType, Actors}

import scala.util.{Success, Try}

object GetMealByDateController {
  def apply(implicit system: ActorSystem[_]): Route = new GetMealByDateController().route()

}

@Path("/api/v1/meal/date")
class GetMealByDateController(implicit system: ActorSystem[_]) extends BaseAuthenticatedController {

  @GET
  @Operation(summary = "Get meal by date", tags = Array("meal"),
    parameters = Array(
      new Parameter(name = "date", in = ParameterIn.QUERY, required = true, description = "Date", content = Array(new Content(schema = new Schema(implementation = classOf[String]))))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[MealDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    parameters("date") { date =>
      authenticatedRoute { userId =>
        val command = Try(GetMealsByDateCommand(userId, date)).transform(Success(_),
          e => throw ExceptionWithResponseCode400(e.getMessage))
        val result = Actors.getActorRef(ActorType.MEAL_DATABASE).ask(ref => Command(command.get, ref))
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
}