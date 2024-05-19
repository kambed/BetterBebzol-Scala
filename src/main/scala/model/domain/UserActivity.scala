package model.domain

object UserActivity extends Enumeration {
  type HowActive = Value

  val SEDENTARY = Value("sedentary")
  val LITTLE = Value("little")
  val MODERATE = Value("moderate")
  val ACTIVE = Value("active")
  val VERY_ACTIVE = Value("very_active")
}