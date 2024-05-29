package rest.api.controller.user

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.GetUserProfileCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode401
import model.domain.UserProfile
import model.dto.UserProfileDto
import rest.api.controller.BaseController
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

import scala.concurrent.Future

object GetUserRecommendationsController {
  def apply(implicit system: ActorSystem[_]): Route = new GetUserRecommendationsController().route()
}

@Path("/api/v1/user/recommendations")
class GetUserRecommendationsController(implicit system: ActorSystem[_]) extends BaseController {

  @GET
  @Operation(summary = "Get user recommendations", tags = Array("user"),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[UserProfileDto])))),
      new ApiResponse(responseCode = "401", description = "Unauthorized"),
      new ApiResponse(responseCode = "403", description = "Forbidden"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    TokenAuthorization.authenticated { claims =>
      val email = claims.get("email")
      if (email.isEmpty) {
        throw ExceptionWithResponseCode401("User not found")
      }
      val result: Future[Command] = Actors.getActorRef(ActorType.PROFILE_SERVICE).ask(ref => Command(GetUserProfileCommand(email.get.toString), ref))
      onSuccess(result) { result: Command =>
        result.command match {
          case returnCommand: ReturnCommand => returnCommand.response match {
            case profile: UserProfile => complete(StatusCodes.OK, profile.toUserProfileDto)
            case other => completeNegative(other)
          }
          case other => completeNegative(other)
        }
      }
    }
  }
}
