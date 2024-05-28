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
}

case class Command(command: BaseCommand, replyTo: ActorRef[Command] = null) extends BaseCommand {

  @Hidden
  def getLastDelayedRequestAndRemove: Command = {
    if (delayedRequests.isEmpty) {
      this
    } else {
      val last = delayedRequests.last
      delayedRequests.remove(delayedRequests.size - 1)
      last
    }
  }

  @Hidden
  def getFirstDelayedRequestAndRemoveAll: Command = {
    if (delayedRequests.isEmpty) {
      this
    } else {
      val head = delayedRequests.head
      delayedRequests.clear()
      head
    }
  }
}