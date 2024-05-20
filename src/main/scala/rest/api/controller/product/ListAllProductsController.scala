package rest.api.controller.product

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.ws.rs.{GET, Path}
import model.command.ListAllProductsCommand
import model.command.abstracts.Command
import model.domain.Product
import model.dto.ProductListDto
import rest.api.controller.BaseController
import util.{ActorType, Actors}

import scala.concurrent.Future

object ListAllProductsController {
  def apply(implicit system: ActorSystem[_]): Route = new ListAllProductsController().route()
}

@Path("/api/v1/product")
class ListAllProductsController(implicit system: ActorSystem[_]) extends BaseController {

  @GET
  @Operation(summary = "List all products", tags = Array("product"),
    responses = Array(
      new ApiResponse(responseCode = "200", content = Array(new Content(schema = new Schema(implementation = classOf[ProductListDto])))),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def route(): Route = get {
    val actorRef = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
    val result: Future[Any] = actorRef.ask(ref => Command(ListAllProductsCommand(), ref))
    onSuccess(result) {
      case products: Seq[Product] => complete(StatusCodes.OK, ProductListDto(products.map(_.toProductDto).toList))
      case other => completeNegative(other)
    }
  }
}
