package service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import model.command.abstracts.{Command, ReturnCommand}
import model.command.exception.ExceptionWithResponseCode400
import model.command.{GetUserCommand, GetUserProfileCommand}
import model.domain.enums.{UserActivity, UserSex}
import model.domain.{User, UserProfile}
import util.{ActorType, Actors}

object ProfileService {
  def apply(): Behavior[Command] = Behaviors.setup(context => new ProfileService(context))
}

private class ProfileService(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  private val actorRef = Actors.getActorRef(ActorType.USER_DATABASE)

  override def onMessage(msg: Command): Behavior[Command] = {
    context.log.info(s"Received message: $msg")
    msg match {
      case command: Command =>
        command.command match {
          case getUserProfileCommand: GetUserProfileCommand => handleGetUserProfileCommand(command, getUserProfileCommand)
          case returnCommand: ReturnCommand => handleReturnCommand(command, returnCommand)
        }
    }
    this
  }

  private def handleGetUserProfileCommand(command: Command, getUserProfileCommand: GetUserProfileCommand): Unit = {
    val commandNew = Command(GetUserCommand(getUserProfileCommand.email), context.self)
    commandNew.addDelayedRequest(Command(getUserProfileCommand, command.replyTo))
    actorRef ! commandNew
  }

  private def handleReturnCommand(command: Command, returnCommand: ReturnCommand): Unit = {
    val headDelayedRequest = command.getLastDelayedRequestAndRemove
    headDelayedRequest.command match {
      case _: GetUserProfileCommand => handleReturnGetUserProfileCommand(headDelayedRequest, returnCommand)
      case _ => headDelayedRequest.replyTo ! Command(returnCommand)
    }
  }

  private def handleReturnGetUserProfileCommand(headDelayedRequest: Command, returnCommand: ReturnCommand): Unit = {
    val user = returnCommand.response.asInstanceOf[User]
    if (user.sex.isEmpty || user.age.isEmpty || user.height.isEmpty || user.weight.isEmpty || user.howActive.isEmpty || user.howActive.isEmpty) {
      throw ExceptionWithResponseCode400("User profile is incomplete, recommendations cannot be calculated")
    }
    val recommendedCalories = calculateRecommendedCalories(calculateBMR(user), user.howActive.get)
    val recommendedProtein = calculateRecommendedProtein(user.weight.get, user.howActive.get)
    val recommendedFat = calculateRecommendedFat(user.weight.get)
    val recommendedCarbohydrates = calculateRecommendedCarbohydrates(recommendedCalories)
    headDelayedRequest.replyTo ! Command(ReturnCommand(
      UserProfile(user.userId, user.email, recommendedCalories, recommendedProtein, recommendedFat, recommendedCarbohydrates)
    ))
  }

  private def calculateBMR(user: User): Double = {
    if (user.sex.get == UserSex.MALE) {
      66.47 + (13.75 * user.weight.get) + (5.003 * user.height.get) - (6.75 * user.age.get)
    } else {
      655.1 + (9.563 * user.weight.get) + (1.850 * user.height.get) - (4.676 * user.age.get)
    }
  }

  private def calculateRecommendedCalories(bmr: Double, howActive: UserActivity.HowActive): Int = {
    (howActive match {
      case UserActivity.SEDENTARY => bmr * 1.2
      case UserActivity.LITTLE => bmr * 1.375
      case UserActivity.MODERATE => bmr * 1.55
      case UserActivity.ACTIVE => bmr * 1.725
      case UserActivity.VERY_ACTIVE => bmr * 1.9
    }).toInt
  }

  private def calculateRecommendedProtein(weight: Int, howActive: UserActivity.HowActive): Double = {
    roundToTwoDecimalPlaces(howActive match {
      case UserActivity.SEDENTARY => weight * 1.0
      case UserActivity.LITTLE => weight * 1.2
      case UserActivity.MODERATE => weight * 1.4
      case UserActivity.ACTIVE => weight * 1.8
      case UserActivity.VERY_ACTIVE => weight * 2.2
    })
  }

  private def calculateRecommendedFat(weight: Int): Double = {
    roundToTwoDecimalPlaces(weight)
  }

  private def calculateRecommendedCarbohydrates(recommendedCalories: Double): Double = {
    roundToTwoDecimalPlaces((recommendedCalories * 0.55) / 4)
  }

  private def roundToTwoDecimalPlaces(value: Double): Double = {
    BigDecimal(value).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }
}
