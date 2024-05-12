package model.command.abstracts

import akka.actor.typed.ActorRef

class BaseCommand

case class Command(command: BaseCommand, replyTo: ActorRef[Any])
