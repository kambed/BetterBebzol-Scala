package rest.api.controller.login

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.{Consumes, PUT, Path, Produces}
import model.command.EditUserPasswordCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode401, ExceptionWithResponseCode403}
import rest.api.controller.BaseController
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

import scala.concurrent.Future

object EditUserPasswordController {
  def apply(implicit system: ActorSystem[_]): Route = new EditUserPasswordController().route()
}

@Path("/api/v1/user/login")
class EditUserPasswordController(implicit system: ActorSystem[_]) extends BaseController {

  @PUT
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Edit user password", tags = Array("login"),
    requestBody = new RequestBody(required = true,
      content = Array(new Content(schema = new Schema(implementation = classOf[EditUserPasswordCommand])))),
    responses = Array(
      new ApiResponse(responseCode = "204", description = "No content"),
      new ApiResponse(responseCode = "400", description = "Bad request"),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "403", description = "Forbidden"),
      new ApiResponse(responseCode = "404", description = "User not found"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = put {
    TokenAuthorization.authenticated { claims =>
      entity(as[EditUserPasswordCommand]) { editUserPasswordCommand =>
        val email = claims.get("email")
        if (email.isEmpty) {
          throw ExceptionWithResponseCode401("User not found")
        }
        if (email.get != editUserPasswordCommand.email) {
          throw ExceptionWithResponseCode403("User can only edit their own account")
        }
        val result: Future[Command] = Actors.getActorRef(ActorType.AUTH_SERVICE).ask(ref => Command(editUserPasswordCommand, ref))
        onSuccess(result) { result: Command =>
          result.command match {
            case returnCommand: ReturnCommand => returnCommand.response match {
              case _: String => complete(StatusCodes.NoContent)
              case other => completeNegative(other)
            }
            case other => completeNegative(other)
          }
        }
      }
    }
  }
}
