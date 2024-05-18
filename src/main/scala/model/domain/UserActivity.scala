package model.domain

object UserActivity extends Enumeration {
  type HowActive = Value

  val sedentary, little, moderate, active, veryActive = Value
}