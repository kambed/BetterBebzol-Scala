package model.domain

import model.domain.UserActivity.HowActive
import model.domain.UserSex.Sex
import model.dto.UserDto

case class User(userId: Long, email: String, password: String, sex: Option[Sex], age: Option[Int],
                height: Option[Int], weight: Option[Int], howActive: Option[HowActive]) {
  def toUserDto: UserDto = UserDto(userId, email, password,
    if (sex.isEmpty) null else sex.map(_.toString),
    age, height, weight,
    if (howActive.isEmpty) null else howActive.map(_.toString))
}