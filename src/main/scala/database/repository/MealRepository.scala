package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.MealTable
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode403, ExceptionWithResponseCode404}
import model.command.meal.{CreateMealCommand, EditMealCommand, GetAllUserMealsCommand, GetMealByIdCommand, GetMealsByDateCommand}
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
      case getMealByIdCommand: GetMealByIdCommand => handleGetMealByIdCommand(getMealByIdCommand, msg)
      case getAllUserMealsCommand: GetAllUserMealsCommand => handleGetAllUserMealsCommand(getAllUserMealsCommand, msg)
      case getMealsByDateCommand: GetMealsByDateCommand => handleGetMealsByDateCommand(getMealsByDateCommand, msg)
      case _ => msg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400("Invalid command")))
    }
    this
  }

  //=====COMMAND HANDLERS===========================================================
  private def handleEditMealCommand(command: EditMealCommand, originalMsg: Command): Unit = {
    checkIfUserIsOwnerOfMeal(command.toMeal.userId, command.mealId).onComplete {
      case Success(isOwner) =>
        if (!isOwner) {
          originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${command.mealId}")))
          return
        }
        updateMeal(command.toMeal).onComplete {
          case Success(meal) =>
            if (meal.isEmpty) {
              originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode404(s"Meal with id ${command.mealId} not found")))
              return
            }
            val response = Command(ReturnCommand(meal.get))
            response.addAllDelayedRequests(originalMsg.delayedRequests)
            originalMsg.replyTo ! response
          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleCreateMealCommand(command: CreateMealCommand, originalMsg: Command): Unit = {
    insertMeal(command.toMeal).onComplete {
      case Success(meal) =>
        val response = Command(ReturnCommand(meal))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) =>
        exception match {
          case exception: SQLIntegrityConstraintViolationException =>
            originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400(exception.getMessage)))
          case _ => originalMsg.replyTo ! Command(ReturnCommand(exception))

      }
    }
  }

  private def handleGetMealByIdCommand(command: GetMealByIdCommand, originalMsg: Command): Unit = {
    checkIfUserIsOwnerOfMeal(command.userId, command.mealId).onComplete {
      case Success(isOwner) =>
        if (!isOwner) {
          originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${command.mealId}")))
          return
        }
        getMealById(command.mealId).onComplete {
          case Success(meal) =>
            if (meal.isEmpty) {
              originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode404(s"Meal with id ${command.mealId} not found")))
              return
            }
            val response = Command(ReturnCommand(meal.get))
            response.addAllDelayedRequests(originalMsg.delayedRequests)
            originalMsg.replyTo ! response
          case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
        }
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetAllUserMealsCommand(command: GetAllUserMealsCommand, originalMsg: Command): Unit = {
    MySQLConnection.db.run(table.filter(_.userId === command.userId).result).onComplete {
      case Success(meals) =>
        val response = Command(ReturnCommand(meals))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetMealsByDateCommand(command: GetMealsByDateCommand, originalMsg: Command): Unit = {
    if (command.date.isEmpty) {
      originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode400("Date is required")))
      return
    }
    if (!command.date.matches("\\d{2}/\\d{2}/\\d{4}")) {
      originalMsg.replyTo ! Command(ReturnCommand(ExceptionWithResponseCode404("Invalid date format")))
      return
    }
    getMealsByUserIdAndDate(command.userId, command.date).onComplete {
      case Success(meals) =>
        val response = Command(ReturnCommand(meals))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  //=====DATABASE METHODS===========================================================
  private def insertMeal(meal: Meal): Future[Meal] = {
    MySQLConnection.db.run((table returning table.map(_.mealId) into ((meal, id) => meal.copy(mealId = id))) += meal)
  }

  private def updateMeal(meal: Meal): Future[Option[Meal]] = {
    getMealById(meal.mealId).flatMap {
      case Some(dbMeal) =>
        val modifiedMean = meal.copy(mealId = dbMeal.mealId)
        MySQLConnection.db.run(table.filter(_.mealId === meal.mealId).update(modifiedMean)).map(_ => Some(modifiedMean))
      case None => Future.successful(None)
    }
  }

  private def getMealById(id: Long): Future[Option[Meal]] = {
    MySQLConnection.db.run(table.filter(_.mealId === id).result.headOption)
  }

  private def getMealsByUserIdAndDate(userId: Long, date: String): Future[Seq[Meal]] = {
    MySQLConnection.db.run(table.filter(_.userId === userId).filter(_.date === date).result)
  }

  //=====LOGIC METHODS===========================================================
  private def checkIfUserIsOwnerOfMeal(userId: Long, mealId: Long): Future[Boolean] = {
    getMealById(mealId).map {
      case Some(meal) => meal.userId == userId
      case None => false
    }
  }
}