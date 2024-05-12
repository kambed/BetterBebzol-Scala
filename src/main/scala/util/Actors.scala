package util

import akka.actor.typed.{ActorRef, ActorSystem}
import model.command.abstracts.Command

import scala.collection.mutable
import scala.collection.mutable.HashMap

sealed abstract class ActorType(val name: String)

object ActorType {
  case object PRODUCT_SERVICE extends ActorType("product-service")
}

object Actors {
  private val actorRefs: mutable.HashMap[ActorType, ActorRef[Command]] = new mutable.HashMap()

  def getActorRef(actorType: ActorType): ActorRef[Command] = {
    actorRefs.getOrElse(actorType, throw new IllegalArgumentException(s"ActorRef for ${actorType.name} not found"))
  }
  
  def addActorRef(actorType: ActorType, actorRef: ActorRef[Command]): Unit = {
    actorRefs.addOne(actorType, actorRef)
  }
  
  def removeActorRef(actorType: ActorType): Unit = {
    actorRefs.remove(actorType)
  }
}
