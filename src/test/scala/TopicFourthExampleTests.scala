package p.lodz.pl

import org.scalatest.funspec.AnyFunSpec

class TopicFourthExampleTests extends AnyFunSpec {

  describe("Arrays") {
    it("should produce NoSuchElementException when head is invoked") {
      assertThrows[NoSuchElementException] {
        Set.empty.head
      }
    }
  }

  describe("Sequences") {
    it("should produce NoSuchElementException when head is invoked") {
      assertThrows[NoSuchElementException] {
        Seq.empty.head
      }
    }
  }

  describe("Lists") {
    it("should produce NoSuchElementException when head is invoked") {
      assertThrows[NoSuchElementException] {
        List.empty.head
      }
    }
  }

  describe("Tuples") {
    it("should produce NoSuchElementException when head is invoked") {
      assertThrows[NoSuchElementException] {
        Map.empty.head
      }
    }
  }

}
