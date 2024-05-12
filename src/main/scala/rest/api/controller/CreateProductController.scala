package rest.api.controller

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import model.command.CreateProductCommand
import model.command.abstracts.Command
import util.{ActorType, Actors}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.http.scaladsl.model.StatusCodes
import model.domain.Product

object CreateProductController {
  def apply(implicit system: ActorSystem[_]): Route = new CreateProductController().route()
}

class CreateProductController(implicit system: ActorSystem[_]) extends BaseController {
  def route(): Route = post {
    entity(as[CreateProductCommand]) { createProductCommand =>
      val actorRef = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
      val result = actorRef.ask(ref => Command(createProductCommand, ref))
      onSuccess(result) {
        case product: Product => complete(StatusCodes.Created, product.toProductDto)
        case other => completeNegative(other)
      }
    }
  }
}
