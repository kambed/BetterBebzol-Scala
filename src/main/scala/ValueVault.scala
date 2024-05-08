package p.lodz.pl

class ValueVault(val id: Int) {
  private var _value: Int = 0
  def value: Int = _value
}

object ValueVault {
  def apply(id: Int): ValueVault = new ValueVault(id)
  def apply(id: Int, value: Int): ValueVault = {
    val vault = new ValueVault(id)
    vault._value = value
    vault
  }
  def unapply(vault: ValueVault): Tuple2[Int, Int] = (vault.id, vault.value)
}
