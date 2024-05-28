package rest.api.controller.user

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{POST, Path}
import model.command.CreateUserCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.domain.User
import model.dto.UserDto
import rest.api.controller.BaseController
import util.{ActorType, Actors}

import scala.concurrent.Future

object CreateUserController {
  def apply(implicit system: ActorSystem[_]): Route = new CreateUserController().route()
}

@Path("/api/v1/user")
class CreateUserController(implicit system: ActorSystem[_]) extends BaseController {

  @POST
  @Operation(summary = "Create user", tags = Array("user"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[CreateUserCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "201", content = Array(new Content(schema = new Schema(implementation = classOf[UserDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = post {
    entity(as[CreateUserCommand]) { createUserCommand =>
      val result: Future[Command] = Actors.getActorRef(ActorType.AUTH_SERVICE).ask(ref => Command(createUserCommand, ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case user: User => complete(StatusCodes.Created, user.toUserDto)
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}
