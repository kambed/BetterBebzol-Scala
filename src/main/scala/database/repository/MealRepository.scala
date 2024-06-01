package database.repository

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import database.MySQLConnection
import database.table.MealTable
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.{ExceptionWithResponseCode400, ExceptionWithResponseCode403, ExceptionWithResponseCode404}
import model.command.meal._
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
    (for {
      meal <- getMealById(command.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
      mealUpdated <- updateMeal(mealWithCheck, command.toMeal)
    } yield mealUpdated).onComplete {
      case Success(meal) =>
        val response = Command(ReturnCommand(meal))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleCreateMealCommand(command: CreateMealCommand, originalMsg: Command): Unit = {
    insertMeal(command.toMeal).onComplete {
      case Success(meal) =>
        val response = Command(ReturnCommand(meal))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetMealByIdCommand(command: GetMealByIdCommand, originalMsg: Command): Unit = {
    (for {
      meal <- getMealById(command.mealId)
      mealWithCheck <- checkIfUserIsOwnerOfMeal(command.userId, meal)
    } yield mealWithCheck).onComplete {
      case Success(meal) =>
        val response = Command(ReturnCommand(meal))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetAllUserMealsCommand(command: GetAllUserMealsCommand, originalMsg: Command): Unit = {
    getMealsByUserId(command.userId).onComplete {
      case Success(meals) =>
        val response = Command(ReturnCommand(meals))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def handleGetMealsByDateCommand(command: GetMealsByDateCommand, originalMsg: Command): Unit = {
    getMealsByUserIdAndDate(command.userId, command.date).onComplete {
      case Success(meals) =>
        val response = Command(ReturnCommand(meals))
        response.addAllDelayedRequests(originalMsg.delayedRequests)
        originalMsg.replyTo ! response
      case Failure(exception) => originalMsg.replyTo ! Command(ReturnCommand(exception))
    }
  }

  private def checkIfUserIsOwnerOfMeal(userId: Long, meal: Meal): Future[Meal] = {
    if (meal.userId != userId) {
      return Future.failed(ExceptionWithResponseCode403(s"You are not the owner of meal with id ${meal.mealId}"))
    }
    Future.successful(meal)
  }

  //=====DATABASE METHODS===========================================================
  private def insertMeal(meal: Meal): Future[Meal] = {
    MySQLConnection.db.run((table returning table.map(_.mealId) into ((meal, id) => meal.copy(mealId = id))) += meal)
      .transform(meal => meal,
        exception => exception match {
          case exception: SQLIntegrityConstraintViolationException => ExceptionWithResponseCode400(exception.getMessage)
          case _ => exception
        }
      )
  }

  private def updateMeal(oldMeal: Meal, meal: Meal): Future[Meal] = {
    val modifiedMeal = meal.copy(mealId = oldMeal.mealId)
    MySQLConnection.db.run(table.filter(_.mealId === meal.mealId).update(modifiedMeal)).map(_ => modifiedMeal)
  }

  private def getMealById(id: Long): Future[Meal] = {
    MySQLConnection.db.run(table.filter(_.mealId === id).result.headOption).flatMap {
      case Some(meal) => Future.successful(meal)
      case None => Future.failed(ExceptionWithResponseCode404(s"Meal with id $id not found"))
    }
  }

  private def getMealsByUserIdAndDate(userId: Long, date: String): Future[Seq[Meal]] = {
    MySQLConnection.db.run(table.filter(_.userId === userId).filter(_.date === date).result)
  }

  private def getMealsByUserId(userId: Long): Future[Seq[Meal]] = {
    MySQLConnection.db.run(table.filter(_.userId === userId).result)
  }
}