package model.domain


object MealTime extends Enumeration {
  type MealType = Value

  val BREAKFAST = Value("breakfast")
  val LUNCH = Value("lunch")
  val DINNER = Value("dinner")
  val SNACK = Value("snack")
  val SECOND_BREAKFAST = Value("second_breakfast")
  val SUPPER = Value("supper")

}