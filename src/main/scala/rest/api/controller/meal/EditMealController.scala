package rest.api.controller.meal

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{PUT, Path}
import model.command.meal.{CreateMealCommand, EditMealCommand}
import model.dto.MealDto
import rest.api.controller.BaseController

//object EditMealController {
//  def apply(implicit system: ActorSystem[_]): Route = new EditMealController().route()
//}
//
//@Path("/api/v1/meal")
//class EditMealController(implicit system: ActorSystem[_]) extends BaseController {
//
//  @PUT
//  @Operation(summary = "Edit meal", tags = Array("meal"),
//    requestBody = new RequestBody(required = true,
//      content = Array(new Content(schema = new Schema(implementation = classOf[EditMealCommand])))),
//    responses = Array(
//      new ApiResponse(responseCode = "201", content = Array(new Content(schema = new Schema(implementation = classOf[MealDto])))),
//      new ApiResponse(responseCode = "400", description = "Bad request"),
//      new ApiResponse(responseCode = "500", description = "Internal server error"))
//  )
//  def route(): Route = put {
//
//  }
//}