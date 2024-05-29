package model.domain

import model.domain.enums.UserActivity.HowActive
import model.domain.enums.UserGoal.Goal
import model.domain.enums.UserSex.Sex
import model.dto.UserDto

case class User(userId: Long, email: String, password: String, sex: Option[Sex], age: Option[Int],
                height: Option[Int], weight: Option[Int], howActive: Option[HowActive], goal: Option[Goal]) {
  def toUserDto: UserDto = UserDto(email, if (sex.isEmpty) null else sex.map(_.toString),
    age, height, weight, if (howActive.isEmpty) null else howActive.map(_.toString), if (goal.isEmpty) null else goal.map(_.toString))
}