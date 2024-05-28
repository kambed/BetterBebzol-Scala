package util

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{Behavior, PostStop, Signal}
import database.repository.{MealRepository, ProductRepository, UserRepository}
import model.command.abstracts.Command
import service.AuthService

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
    //DATABASE ACTORS
    Actors.addActorRef(ActorType.PRODUCT_DATABASE, context.spawn(ProductRepository(), ActorType.PRODUCT_DATABASE.name))
    Actors.addActorRef(ActorType.USER_DATABASE, context.spawn(UserRepository(), ActorType.USER_DATABASE.name))
    Actors.addActorRef(ActorType.MEAL_DATABASE, context.spawn(MealRepository(), ActorType.MEAL_DATABASE.name))

    //SERVICE ACTORS
    Actors.addActorRef(ActorType.AUTH_SERVICE, context.spawn(AuthService(), ActorType.AUTH_SERVICE.name))
  }

  private def unregisterActors(): Unit = {
    Actors.getAllActorRefs.keys.foreach(Actors.removeActorRef)
  }

  registerActors()
}
