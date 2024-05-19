package model.domain

object UserSex extends Enumeration {
  type Sex = Value

  val MALE = Value("male")
  val FEMALE = Value("female")
}