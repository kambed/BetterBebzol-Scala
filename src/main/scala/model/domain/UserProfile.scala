package model.domain

import model.dto.UserProfileDto

case class UserProfile(userId: Long, email: String, recommendedCalories: Int, recommendedProtein: Double,
                       recommendedFat: Double, recommendedCarbohydrates: Double) {
  def toUserProfileDto: UserProfileDto = UserProfileDto(email, recommendedCalories, recommendedProtein, recommendedFat, recommendedCarbohydrates)
}