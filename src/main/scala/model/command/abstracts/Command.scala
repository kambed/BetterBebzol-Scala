package model.command.abstracts

import akka.actor.typed.ActorRef

import scala.collection.mutable.ListBuffer

class BaseCommand(var delayedRequests: ListBuffer[Command] = ListBuffer())

case class Command(command: BaseCommand, replyTo: ActorRef[Command] = null) extends BaseCommand