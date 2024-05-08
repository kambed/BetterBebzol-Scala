package p.lodz.pl

import org.scalatest.funspec.AnyFunSpec

class TopicThirdExampleTests extends AnyFunSpec {

  describe("LambdaTest") {

    val lambda = (x: Int) => x + 1
    val lambda2: Int => Int = x => x + 1

    it("should simple lambda give expected results") {
      assert(lambda(1) == 2)
      assert(lambda2(1) == 2)
    }

    def function(x: Int): Int = {
      x + 1
    }

    it("should lambda and function be equal") {
      assert(lambda(1) == 2)
      assert(function(1) == 2)
      assert(lambda(1) == function(1))
    }

    val lambdaToVal: Int => Int = lambda
    val functionToVal: Int => Int = function

    it("should be possible to cast lambda and function to val") {
      assert(lambdaToVal(1) == 2)
      assert(functionToVal(1) == 2)
      assert(lambdaToVal(1) == functionToVal(1))
    }

    val lambdaMultiValue = (x: Int, y: Int) => x + y

    it("should multiple value lambda give expected results") {
      assert(lambdaMultiValue(1, 2) == 3)
    }
  }

  describe("LambdaAsFunctionParameter") {

    def function(x: Int, y: Int, lambda: (Int, Int) => Int): Int = {
      lambda(x, y)
    }

    it("should lambda be passed as function parameter") {
      assert(function(1, 2, (x, y) => x + y) == 3)
      assert(function(1, 2, (x, y) => x - y) == -1)
    }

    it("should lambda be passed as function parameter with type") {
      val lambda: (Int, Int) => Int = (x, y) => x + y
      assert(function(1, 2, lambda) == 3)
      assert(function(3, 2, lambda) == 5)
    }
  }

  describe("LambdaInCollectionsOperations") {

    it("should lambda be used in map") {
      val list = List(1, 2, 3)
      val result = list.map(x => x + 1)
      assert(result == List(2, 3, 4))
    }

    it("should lambda be used in filter") {
      val list = List(1, 2, 3)
      val result = list.filter(x => x == 2)
      assert(result == List(2))
    }

    it("should lambda be used in sort") {
      val list = List(1, 2, 3)
      val result = list.sortWith((x, y) => x > y)
      assert(result == List(3, 2, 1))
    }
  }

  describe("LambdaSimplification") {

    it("should lambda be used in map") {
      val list = List(1, 2, 3)
      val result = list.map((x: Int) => x + 1)
      assert(result == List(2, 3, 4))
    }

    it("should lambda be used in map without type") {
      val list = List(1, 2, 3)
      val result = list.map(x => x + 1)
      assert(result == List(2, 3, 4))
    }

    it("should lambda be used in map with underscore") {
      val list = List(1, 2, 3)
      val result = list.map(_ + 1)
      assert(result == List(2, 3, 4))
    }
  }

  describe("LambdaClosure") {

      it("should lambda have access to outer scope") {
        val y = 1
        val lambda = (x: Int) => x + y
        assert(lambda(2) == 3)
      }
  }
}
