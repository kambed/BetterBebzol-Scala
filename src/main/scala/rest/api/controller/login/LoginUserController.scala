package rest.api.controller.login

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.ws.rs.{POST, Path}
import model.command.user.LoginUserCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.dto.UserTokenDto
import rest.api.controller.BaseController
import util.{ActorType, Actors}

import scala.concurrent.Future

object LoginUserController {
  def apply(implicit system: ActorSystem[_]): Route = new LoginUserController().route()
}

@Path("/api/v1/user/login")
class LoginUserController(implicit system: ActorSystem[_]) extends BaseController {

  @POST
  @Operation(summary = "Login user", tags = Array("login"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[LoginUserCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[UserTokenDto])))),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "404", description = "User not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = post {
    entity(as[LoginUserCommand]) { loginUserCommand =>
      val result: Future[Command] = Actors.getActorRef(ActorType.AUTH_SERVICE).ask(ref => Command(loginUserCommand, ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case jwt: String => complete(StatusCodes.OK, UserTokenDto(jwt))
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}
