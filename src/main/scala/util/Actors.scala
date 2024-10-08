package util

import akka.actor.typed.ActorRef
import model.command.abstracts.Command

import scala.collection.mutable

sealed abstract class ActorType(val name: String)

object ActorType {
  case object PRODUCT_DATABASE extends ActorType("product-database")
  case object USER_DATABASE extends ActorType("user-database")
  case object MEAL_DATABASE extends ActorType("meal-database")

  case object AUTH_SERVICE extends ActorType("auth-service")
  case object PROFILE_SERVICE extends ActorType("profile-service")
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
