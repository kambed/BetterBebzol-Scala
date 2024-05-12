package rest.api.controller

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import model.command.CreateProductCommand
import model.command.abstracts.Command
import util.{ActorType, Actors}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.AskPattern.schedulerFromActorSystem

object CreateProductController {
  def apply(implicit system: ActorSystem[_]): Route = new CreateProductController().route()
}

class CreateProductController(implicit system: ActorSystem[_]) extends BaseController {

  def route(): Route = post {
    entity(as[CreateProductCommand]) { createProductCommand =>
      val actorRef = Actors.getActorRef(ActorType.PRODUCT_SERVICE)
      complete(actorRef.ask(ref => Command(createProductCommand, ref)))
    }
  }
}
