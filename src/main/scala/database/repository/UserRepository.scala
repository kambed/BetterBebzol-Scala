package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.UserTable
import model.command.CreateUserCommand
import model.command.abstracts.Command
import model.command.exception.ExceptionWithResponseCode400
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
          case Success(user) => msg.replyTo ! user
          case Failure(exception) =>
            exception match {
              case exception: SQLIntegrityConstraintViolationException =>
                msg.replyTo ! ExceptionWithResponseCode400(exception.getMessage)
              case _ => msg.replyTo ! exception
            }
        }
    }
    this
  }

  private def insertUser(user: User): Future[User] = {
    MySQLConnection.db.run((table returning table.map(_.userId)) += user).map(id => user.copy(userId = id))
  }
}
