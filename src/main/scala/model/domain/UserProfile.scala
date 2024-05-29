package model.domain

import model.domain.enums.UserGoal.Goal
import model.dto.UserProfileDto

case class UserProfile(userId: Long, email: String, goal: Goal, calories: Int, protein: Double,
                       fat: Double, carbohydrates: Double) {
  def toUserProfileDto: UserProfileDto = UserProfileDto(email, goal.toString, calories, protein, fat, carbohydrates)
}