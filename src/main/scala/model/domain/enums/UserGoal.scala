package model.domain.enums

object UserGoal extends Enumeration {
  type Goal = Value

  val GAIN_WEIGHT = Value("gain_weight")
  val MAINTAIN_WEIGHT = Value("maintain_weight")
  val LOSE_WEIGHT = Value("lose_weight")
}