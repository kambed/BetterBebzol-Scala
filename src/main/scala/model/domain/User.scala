package model.domain

import model.dto.UserDto

case class User(userId: Long, email: String, password: String, age: Int,
                height: Double, weight: Double, howActive: UserActivity.HowActive) {
  def toUserDto: UserDto = UserDto(userId, email, password, age, height, weight, howActive.toString)
}