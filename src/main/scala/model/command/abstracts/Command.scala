package model.command.abstracts

import akka.actor.typed.ActorRef

class BaseCommand

case class Command(command: BaseCommand, replyTo: ActorRef[Any], internalCommand: InternalCommand = null)

case class InternalCommand(internalCommand: BaseCommand, replyInternallyTo: ActorRef[Command])