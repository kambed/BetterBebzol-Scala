package model.command.abstracts

import akka.actor.typed.ActorRef

import scala.collection.mutable.ListBuffer

class BaseCommand(val delayedRequests: ListBuffer[Command] = ListBuffer()) {

  def addDelayedRequest(command: Command): Unit = {
    delayedRequests += command
  }

  def addAllDelayedRequests(commands: ListBuffer[Command]): Unit = {
    delayedRequests ++= commands
  }

  def getFirstDelayedRequestAndRemove: Option[Command] = {
    if (delayedRequests.isEmpty) {
      None
    } else {
      val head = delayedRequests.head
      delayedRequests.remove(0)
      Some(head)
    }
  }
}

case class Command(command: BaseCommand, replyTo: ActorRef[Command] = null) extends BaseCommand