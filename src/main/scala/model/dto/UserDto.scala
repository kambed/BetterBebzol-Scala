package model.dto

case class UserDto(userId: Long, email: String, password: String, age: Option[Int],
                   height: Option[Double], weight: Option[Double], howActive: String)
