package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.UserTable
import model.command.{CreateUserCommand, GetUserCommand, ValidateUserCommand}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode404}
import model.domain.User
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import java.sql.SQLIntegrityConstraintViolationException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object UserRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new UserRepository(context))
}

private class UserRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  lazy val table = TableQuery[UserTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createUserCommand: CreateUserCommand =>
        insertUser(createUserCommand.toUser).onComplete {
          case Success(user) =>
            val response = Command(ReturnCommand(user))
            response.addAllDelayedRequests(msg.delayedRequests)
            msg.replyTo ! response
          case Failure(exception) =>
            exception match {
              case exception: SQLIntegrityConstraintViolationException =>
                msg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400(exception.getMessage)))
              case _ => msg.replyTo ! Command(ReturnCommand(exception))
            }
        }
      case getUserCommand: GetUserCommand =>
        getUserByEmail(getUserCommand.email).onComplete {
          case Success(user) =>
            if (user.isEmpty) {
              msg.getLastDelayedRequestAndRemoveAll.getOrElse(msg).replyTo !
                Command(ReturnCommand(ExceptionWithResponseCode404(s"User with email ${getUserCommand.email} not found")))
              return this
            }
            val response = Command(ReturnCommand(user.get))
            response.addAllDelayedRequests(msg.delayedRequests)
            msg.replyTo ! response
          case Failure(exception) => msg.getLastDelayedRequestAndRemoveAll.getOrElse(msg).replyTo ! Command(ReturnCommand(exception))
        }
    }
    this
  }

  private def getUserByEmail(email: String): Future[Option[User]] = {
    MySQLConnection.db.run(table.filter(_.email === email).result.headOption)
  }

  private def insertUser(user: User): Future[User] = {
    MySQLConnection.db.run((table returning table.map(_.userId)) += user).map(id => user.copy(userId = id))
  }
}
