package model.domain

import model.domain.UserActivity.HowActive
import model.dto.UserDto

case class User(userId: Long, email: String, password: String, age: Option[Int],
                height: Option[Double], weight: Option[Double], howActive: HowActive) {
  def toUserDto: UserDto = UserDto(userId, email, password, age, height, weight, howActive.toString)
}