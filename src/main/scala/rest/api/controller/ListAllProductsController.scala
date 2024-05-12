package rest.api.controller

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import util.{ActorType, Actors}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem
import model.command.ListAllProductsCommand
import model.command.abstracts.Command

object ListAllProductsController {
  def apply(implicit system: ActorSystem[_]): Route = new ListAllProductsController().route()
}

class ListAllProductsController(implicit system: ActorSystem[_]) extends BaseController {
  def route(): Route = get {
    val actorRef = Actors.getActorRef(ActorType.PRODUCT_SERVICE)
    complete(actorRef.ask(ref => Command(ListAllProductsCommand(), ref)))
  }
}
