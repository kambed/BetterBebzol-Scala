package util

import akka.actor.typed.{Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.repository.ProductRepository
import model.command.abstracts.Command

object Supervisor {
  def apply(): Behavior[Command] = {
    Behaviors.setup[Command](context => new Supervisor(context))
  }
}

class Supervisor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  override def onMessage(msg: Command): Behavior[Command] = {
    Behaviors.unhandled
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop => this
  }
  
  private def registerActors(): Unit = {
    Actors.addActorRef(ActorType.PRODUCT_SERVICE, context.spawn(ProductRepository(), ActorType.PRODUCT_SERVICE.name))
  }
  registerActors()
}
