package rest.api.controller.user

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import jakarta.ws.rs.{GET, Path}
import model.command.user.GetUserCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.domain.User
import model.dto.UserDto
import rest.api.controller.BaseController
import util.{ActorType, Actors}

import scala.concurrent.Future

object GetUserController {
  def apply(implicit system: ActorSystem[_], email: String): Route = new GetUserController().route()
}

@Path("/api/v1/user/{email}")
class GetUserController(implicit system: ActorSystem[_], val email: String) extends BaseController {

  @GET
  @Operation(summary = "Get user by email", tags = Array("user"),
    parameters = Array(
      new Parameter(name = "email", in = ParameterIn.PATH, required = true, description = "User email", content = Array(new Content(schema = new Schema(implementation = classOf[String]))))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[UserDto])))),
      new ApiResponse(responseCode = "404", description = "User not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    val result: Future[Command] = Actors.getActorRef(ActorType.USER_DATABASE).ask(ref => Command(GetUserCommand(email), ref))
    onSuccess(result) { result: Command =>
      result.command match {
        case returnCommand: ReturnCommand => returnCommand.response match {
          case user: User => complete(StatusCodes.OK, user.toUserDto)
          case other => completeNegative(other)
        }
        case other => completeNegative(other)
      }
    }
  }
}
