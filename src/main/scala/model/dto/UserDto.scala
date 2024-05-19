package model.dto

case class UserDto(userId: Long,
                   email: String,
                   password: String,
                   sex: Option[String],
                   age: Option[Int],
                   height: Option[Int],
                   weight: Option[Int],
                   howActive: Option[String])
