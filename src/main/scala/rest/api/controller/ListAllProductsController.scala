package rest.api.controller

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import util.{ActorType, Actors}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import akka.http.scaladsl.model.StatusCodes
import model.command.ListAllProductsCommand
import model.command.abstracts.Command
import model.domain.Product

object ListAllProductsController {
  def apply(implicit system: ActorSystem[_]): Route = new ListAllProductsController().route()
}

class ListAllProductsController(implicit system: ActorSystem[_]) extends BaseController {
  def route(): Route = get {
    val actorRef = Actors.getActorRef(ActorType.PRODUCT_DATABASE)
    val result = actorRef.ask(ref => Command(ListAllProductsCommand(), ref))
    onSuccess(result) {
      case products: Seq[Product] => complete(StatusCodes.OK, products.map(_.toProductDto).toList)
      case other => completeNegative(other)
    }
  }
}
