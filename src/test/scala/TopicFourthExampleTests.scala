package p.lodz.pl

import org.scalatest.funspec.AnyFunSpec

class TopicFourthExampleTests extends AnyFunSpec {

  describe("ApplyMethodTests") {

    it("should return the same result as using the apply method") {
      val lambda = (x: Int) => x + 1

      assert(lambda(2) == 3)
      assert(lambda.apply(2) == 3)
      assert(lambda.apply(2) == lambda(2))
    }

    it("should return the same result as using the apply method in object") {
      object OneAdder {
        def apply(x: Int): Int = x + 1
      }

      assert(OneAdder(2) == 3)
      assert(OneAdder.apply(2) == 3)
      assert(OneAdder.apply(2) == OneAdder(2))
    }

    it("should return the same result as using the apply method in class") {
      class OneAdder {
        def apply(x: Int): Int = x + 1
      }

      val oneAdder = new OneAdder
      assert(oneAdder(2) == 3)
      assert(oneAdder.apply(2) == 3)
      assert(oneAdder.apply(2) == oneAdder(2))
    }

    it("should return the same result as using the apply method in case class") {
      case class OneAdderValue(value: Int)

      val oneAdder1 = OneAdderValue(2)
      val oneAdder2 = OneAdderValue.apply(2)

      assert(oneAdder1 == oneAdder2)
    }
  }

  describe("CompanionObjectTests") {
    it("init instance with companion object") {
      val vault = ValueVault(2)
      assert(vault.id == 2)
      assert(vault.value == 0)

      val vault2 = ValueVault(2, 3)
      assert(vault2.id == 2)
      assert(vault2.value == 3)
    }

    it("companion object extractor") {
      val vault = ValueVault(2, 3)

      val (id, value) = ValueVault.unapply(vault)
      assert(id == 2)
      assert(value == 3)
    }
  }

}
