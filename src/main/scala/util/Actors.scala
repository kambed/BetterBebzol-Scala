package util

import akka.actor.typed.ActorRef
import model.command.abstracts.Command

import scala.collection.mutable

sealed abstract class ActorType(val name: String)

object ActorType {
  case object PRODUCT_DATABASE extends ActorType("product-database")
}

object Actors {
  private val actorRefs: mutable.HashMap[ActorType, ActorRef[Command]] = new mutable.HashMap()

  def getAllActorRefs: Map[ActorType, ActorRef[Command]] = actorRefs.toMap

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
