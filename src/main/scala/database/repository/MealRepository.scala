package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.MealTable
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode404}
import model.command.meal.{CreateMealCommand, EditMealCommand}
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
      case editMealCommand: EditMealCommand => handleEditMealCommand(editMealCommand, msg)
      case createMealCommand: CreateMealCommand => handleCreateMealCommand(createMealCommand, msg)
    }
    this
  }

  private def handleEditMealCommand(command: EditMealCommand, originalMsg: Command): Unit = {
    updateMeal(command.toMeal).onComplete {
      case Success(mealOpt) => respondToEditMealCommand(mealOpt, command, originalMsg)
      case Failure(exception) => respondWithFailure(exception, originalMsg)
    }
  }

  private def handleCreateMealCommand(command: CreateMealCommand, originalMsg: Command): Unit = {
    insertMeal(command.toMeal).onComplete {
      case Success(meal) => respondWithSuccess(meal, originalMsg)
      case Failure(exception) => handleCreateMealFailure(exception, originalMsg)
    }
  }

  private def insertMeal(meal: Meal): Future[Meal] = {
    MySQLConnection.db.run((table returning table.map(_.mealId) into ((meal, id) => meal.copy(mealId = id))) += meal)
  }

  private def updateMeal(meal: Meal): Future[Option[Meal]] = {
    MySQLConnection.db.run(table.filter(_.mealId === meal.mealId).update(meal)).map {
      case 0 => None
      case _ => Some(meal)
    }
  }

  private def respondToEditMealCommand(mealOpt: Option[Meal], command: EditMealCommand, originalMsg: Command): Unit = {
    if (mealOpt.isEmpty) {
      originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode404(s"Meal with id ${command.mealId} not found")))
    } else {
      val response = Command(ReturnCommand(mealOpt.get))
      response.addAllDelayedRequests(originalMsg.delayedRequests)
      originalMsg.replyTo ! response
    }
  }

  private def respondWithSuccess(meal: Meal, originalMsg: Command): Unit = {
    val response = Command(ReturnCommand(meal))
    response.addAllDelayedRequests(originalMsg.delayedRequests)
    originalMsg.replyTo ! response
  }

  private def respondWithFailure(exception: Throwable, originalMsg: Command): Unit = {
    originalMsg.replyTo ! Command(ReturnCommand(exception))
  }

  private def handleCreateMealFailure(exception: Throwable, originalMsg: Command): Unit = {
    exception match {
      case exception: SQLIntegrityConstraintViolationException =>
        originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400(exception.getMessage)))
      case _ => respondWithFailure(exception, originalMsg)
    }
  }
}