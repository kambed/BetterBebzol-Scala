package util

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
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
    case PostStop =>
      unregisterActors()
      this
  }
  
  private def registerActors(): Unit = {
    Actors.addActorRef(ActorType.PRODUCT_DATABASE, context.spawn(ProductRepository(), ActorType.PRODUCT_DATABASE.name))
  }

  private def unregisterActors(): Unit = {
    Actors.getAllActorRefs.keys.foreach(Actors.removeActorRef)
  }

  registerActors()
}
