package model.command.abstracts

import akka.actor.typed.ActorRef
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import io.swagger.v3.oas.annotations.Hidden

import scala.collection.mutable.ListBuffer

@JsonIgnoreProperties(Array("delayedRequests"))
class BaseCommand(val delayedRequests: ListBuffer[Command] = ListBuffer()) {

  def addDelayedRequest(command: Command): Unit = {
    delayedRequests += command
  }

  def addAllDelayedRequests(commands: ListBuffer[Command]): Unit = {
    delayedRequests ++= commands
  }

  @Hidden
  def getFirstDelayedRequestAndRemove: Option[Command] = {
    if (delayedRequests.isEmpty) {
      None
    } else {
      val head = delayedRequests.head
      delayedRequests.remove(0)
      Some(head)
    }
  }

  @Hidden
  def getLastDelayedRequestAndRemoveAll: Option[Command] = {
    if (delayedRequests.isEmpty) {
      None
    } else {
      val last = delayedRequests.last
      delayedRequests.clear()
      Some(last)
    }
  }
}

case class Command(command: BaseCommand, replyTo: ActorRef[Command] = null) extends BaseCommand