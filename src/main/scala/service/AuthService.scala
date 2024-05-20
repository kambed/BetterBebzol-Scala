package service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.command.abstracts.{Command, InternalCommand}
import model.command.exception.ExceptionWithResponseCode400
import model.command.{CreateUserCommand, GetUserCommand, LoginUserCommand, ValidateUserCommand}
import util.hash.BCryptHelper
import util.jwt.TokenAuthorization
import util.{ActorType, Actors}

object AuthService {
  def apply(): Behavior[Any] = Behaviors.setup(context => new AuthService(context))
}

private class AuthService(context: ActorContext[Any]) extends AbstractBehavior[Any](context) {

  private val actorRef = Actors.getActorRef(ActorType.USER_DATABASE)

  override def onMessage(msg: Any): Behavior[Any] = {
    context.log.info(s"Received message: $msg")
    msg match {
      case command: Command =>
        command.command match {
          case createUserCommand: CreateUserCommand =>
            val hashedCreateUserCommand = createUserCommand.copy(password = BCryptHelper.hashPassword(createUserCommand.password))
            actorRef ! Command(hashedCreateUserCommand, command.replyTo)
          case loginUserCommand: LoginUserCommand =>
            actorRef ! Command(GetUserCommand(loginUserCommand.email), command.replyTo,
              InternalCommand(ValidateUserCommand(loginUserCommand.password, null), context.self))
          case validateUserCommand: ValidateUserCommand =>
            if (BCryptHelper.checkPassword(validateUserCommand.givenPassword, validateUserCommand.user.password)) {
              command.replyTo ! TokenAuthorization.generateToken(validateUserCommand.user)
            } else {
              command.replyTo ! ExceptionWithResponseCode400("Invalid password")
            }
        }
    }
    this
  }
}
