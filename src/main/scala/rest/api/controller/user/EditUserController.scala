package rest.api.controller.user

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{PUT, Path}
import model.command.user.EditUserCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode401, ExceptionWithResponseCode403}
import model.domain.User
import model.dto.UserDto
import rest.api.controller.BaseController
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

import scala.concurrent.Future

object EditUserController {
  def apply(implicit system: ActorSystem[_]): Route = new EditUserController().route()
}

@Path("/api/v1/user")
class EditUserController(implicit system: ActorSystem[_]) extends BaseController {

  @PUT
  @Operation(summary = "Edit user", tags = Array("user"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[EditUserCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[UserDto])))),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "403", description = "Forbidden"),
      new ApiResponse(responseCode = "404", description = "User not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = put {
    TokenAuthorization.authenticated { claims =>
      entity(as[EditUserCommand]) { editUserCommand =>
        val email = claims.get("email")
        if (email.isEmpty) {
          throw ExceptionWithResponseCode401("User not found")
        }
        if (email.get != editUserCommand.email) {
          throw ExceptionWithResponseCode403("User can only edit their own account")
        }
        val result: Future[Command] = Actors.getActorRef(ActorType.USER_DATABASE).ask(ref => Command(editUserCommand, ref))
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
  }
}
