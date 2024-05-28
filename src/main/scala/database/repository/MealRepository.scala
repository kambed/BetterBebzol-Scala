package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.MealTable
import model.command.CreateMealCommand
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode400
import model.domain.Meal
import slick.jdbc.MySQLProfile.api._
import slick.lifted.TableQuery

import java.sql.SQLIntegrityConstraintViolationException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object MealRepository {
  def apply(): Behavior[Command] = Behaviors.setup(context => new MealRepository(context))
}

private class MealRepository(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {
  lazy val table = TableQuery[MealTable]

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg.command match {
      case createMealCommand: CreateMealCommand =>
        insertMeal(createMealCommand.toMeal).onComplete {
          case Success(meal) =>
            val response = Command(ReturnCommand(meal))
            response.addAllDelayedRequests(msg.delayedRequests)
            msg.replyTo ! response
          case Failure(exception) =>
            exception match {
              case exception: SQLIntegrityConstraintViolationException =>
                msg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400(exception.getMessage)))
              case _ => msg.replyTo ! Command(ReturnCommand(exception))
            }
        }
    }
    this
  }

  private def insertMeal(meal: Meal): Future[Meal] = {
    MySQLConnection.db.run((table returning table.map(_.mealId) into ((meal, id) => meal.copy(mealId = id))) += meal)
  }
}