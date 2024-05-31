package service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode401
import model.command.user.{CreateUserCommand, EditUserPasswordCommand, GetUserCommand, LoginUserCommand}
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
        command.command match {
          case createUserCommand: CreateUserCommand => handleCreateUserCommand(command, createUserCommand)
          case loginUserCommand: LoginUserCommand => handleLoginUserCommand(command, loginUserCommand)
          case editUserPasswordCommand: EditUserPasswordCommand => handleEditUserPasswordCommand(command, editUserPasswordCommand)
          case returnCommand: ReturnCommand => handleReturnCommand(command, returnCommand)
        }
    }
    this
  }

  private def handleCreateUserCommand(command: Command, createUserCommand: CreateUserCommand): Unit = {
    val hashedCreateUserCommand = createUserCommand.copy(password = BCryptHelper.hashPassword(createUserCommand.password))
    actorRef ! Command(hashedCreateUserCommand, command.replyTo)
  }

  private def handleLoginUserCommand(command: Command, loginUserCommand: LoginUserCommand): Unit = {
    val commandNew = Command(GetUserCommand(loginUserCommand.email), context.self)
    commandNew.addDelayedRequest(Command(loginUserCommand, command.replyTo))
    actorRef ! commandNew
  }

  private def handleEditUserPasswordCommand(command: Command, editUserPasswordCommand: EditUserPasswordCommand): Unit = {
    val hashedEditUserPasswordCommand = editUserPasswordCommand.copy(password = BCryptHelper.hashPassword(editUserPasswordCommand.password))
    actorRef ! Command(hashedEditUserPasswordCommand, command.replyTo)
  }

  private def handleReturnCommand(command: Command, returnCommand: ReturnCommand): Unit = {
    val headDelayedRequest = command.getLastDelayedRequestAndRemove
    headDelayedRequest.command match {
      case loginUserCommand: LoginUserCommand => handleReturnLoginUserCommand(headDelayedRequest, returnCommand, loginUserCommand)
      case _ => headDelayedRequest.replyTo ! Command(returnCommand)
    }
  }

  private def handleReturnLoginUserCommand(headDelayedRequest: Command, returnCommand: ReturnCommand, loginUserCommand: LoginUserCommand): Unit = {
    val user = returnCommand.response.asInstanceOf[User]
    if (BCryptHelper.checkPassword(loginUserCommand.password, user.password)) {
      headDelayedRequest.replyTo ! Command(ReturnCommand(TokenAuthorization.generateToken(user)))
    } else {
      headDelayedRequest.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode401("Invalid email or password")))
    }
  }
}
