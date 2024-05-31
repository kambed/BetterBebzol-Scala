package rest.api.controller.user

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.user.GetUserCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode401
import model.domain.User
import model.dto.UserDto
import rest.api.controller.BaseController
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

import scala.concurrent.Future

object GetLoggedUserController {
  def apply(implicit system: ActorSystem[_]): Route = new GetLoggedUserController().route()
}

@Path("/api/v1/user/")
class GetLoggedUserController(implicit system: ActorSystem[_]) extends BaseController {

  @GET
  @Operation(summary = "Get logged in user", tags = Array("user"),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[UserDto])))),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    TokenAuthorization.authenticated { claims =>
      val email = claims.get("email")
      if (email.isEmpty) {
        throw ExceptionWithResponseCode401("User not found")
      }
      val result: Future[Command] = Actors.getActorRef(ActorType.USER_DATABASE).ask(ref => Command(GetUserCommand(email.get.toString), ref))
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
