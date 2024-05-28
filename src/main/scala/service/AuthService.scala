package service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode401
import model.command.{CreateUserCommand, EditUserPasswordCommand, GetUserCommand, LoginUserCommand}
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
        val headDelayedRequest = command.getLastDelayedRequestAndRemove
        command.command match {
          case createUserCommand: CreateUserCommand =>
            val hashedCreateUserCommand = createUserCommand.copy(password = BCryptHelper.hashPassword(createUserCommand.password))
            actorRef ! Command(hashedCreateUserCommand, command.replyTo)
          case loginUserCommand: LoginUserCommand =>
            val command = Command(GetUserCommand(loginUserCommand.email), context.self)
            command.addDelayedRequest(Command(loginUserCommand, msg.replyTo))
            actorRef ! command
          case editUserPasswordCommand: EditUserPasswordCommand =>
            val hashedEditUserPasswordCommand = editUserPasswordCommand.copy(password = BCryptHelper.hashPassword(editUserPasswordCommand.password))
            actorRef ! Command(hashedEditUserPasswordCommand, command.replyTo)
          case returnCommand: ReturnCommand =>
            headDelayedRequest.command match {
              case loginUserCommand: LoginUserCommand =>
                val user = returnCommand.response.asInstanceOf[User]
                if (BCryptHelper.checkPassword(loginUserCommand.password, user.password)) {
                  headDelayedRequest.replyTo ! Command(ReturnCommand(TokenAuthorization.generateToken(user)))
                } else {
                  headDelayedRequest.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode401("Invalid email or password")))
                }
              case _ => headDelayedRequest.replyTo ! Command(returnCommand)
            }
        }
    }
    this
  }
}
