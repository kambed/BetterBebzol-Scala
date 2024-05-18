package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.UserTable
import model.command.CreateUserCommand
import model.command.abstracts.Command
import model.domain.User
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object UserRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new UserRepository(context))
}

private class UserRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  lazy val table = TableQuery[UserTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createUserCommand: CreateUserCommand =>
        insertUser(createUserCommand.toUser).onComplete(msg.replyTo ! _.get)
    }
    this
  }

  private def insertUser(user: User): Future[User] = {
    MySQLConnection.db.run((table returning table.map(_.user_id)) += user).map(id => user.copy(userId = id))
  }
}
