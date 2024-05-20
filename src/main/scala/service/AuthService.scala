package service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode400
import model.command.{CreateUserCommand, GetUserCommand, LoginUserCommand, ValidateUserCommand}
import model.domain.User
import util.hash.BCryptHelper
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

object AuthService {
  def apply(): Behavior[Command] = Behaviors.setup(context => new AuthService(context))
}

private class AuthService(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  private val actorRef = Actors.getActorRef(ActorType.USER_DATABASE)

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg match {
      case command: Command =>
        var headRequest = Command(null, null)
        if (command.delayedRequests.nonEmpty) {
          headRequest = command.delayedRequests.head
          command.delayedRequests.drop(1)
        }
        command.command match {
          case createUserCommand: CreateUserCommand =>
            val hashedCreateUserCommand = createUserCommand.copy(password = BCryptHelper.hashPassword(createUserCommand.password))
            actorRef ! Command(hashedCreateUserCommand, command.replyTo)
          case loginUserCommand: LoginUserCommand =>
            val command = Command(GetUserCommand(loginUserCommand.email), context.self)
            command.delayedRequests += Command(loginUserCommand, msg.replyTo)
            actorRef ! command
          case returnCommand: ReturnCommand =>
            headRequest.command match {
              case loginUserCommand: LoginUserCommand =>
                val user = returnCommand.response.asInstanceOf[User]
                if (BCryptHelper.checkPassword(loginUserCommand.password, user.password)) {
                  headRequest.replyTo ! Command(ReturnCommand(TokenAuthorization.generateToken(user)))
                } else {
                  headRequest.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400("Invalid password")))
                }
            }
        }
    }
    this
  }
}
