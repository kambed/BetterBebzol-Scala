package p.lodz.pl

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.funspec.AnyFunSpec

class TopicSecondExampleTests extends AnyFlatSpec {


  "For loop" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    var sum = 0
    for (i <- list) {
      sum += i
    }
    assert(sum == 15)
  }

  "For loop2" should "work" in {
    for (i <- 1 to 10) {
      println(i)
    }
    println("----")

    for (i <- 1 until 10) {
      println(i)
    }
    println("----")

    for (i <- 1 to 10 by 2) {
      println(i)
    }
    println("----")
  }

  "For loop with condition" should "work" in {
    for (i <- 1 to 10 if i % 2 == 0) {
      println(i)
    }
  }

  "For loop with yield" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    val result = for (i <- list) yield i * 2 // yield is like map in scala creating new list
    assert(result == List(2, 4, 6, 8, 10))
  }

  "While loop" should "work" in {
    var i = 0
    while (i < 10) {
      println(i)
      i += 1
    }
  }

  "Do while loop" should "work" in {
    var i = 0
    while {
      i += 1
      i < 5
    } do {
      println(i)
    }

    println("----")
    var set = 0
    while ( {
      set += 1
      set < 32
    }) {
      println(set)
    }
  }

  "Foreach loop" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    list.foreach(println)
  }

  "Foreach loop2" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    list.foreach(i => println(i))
  }

  "Foreach loop3" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    list.foreach(i => {
      println(i)
    })
  }

  "Foreach loop with index" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    list.zipWithIndex.foreach {
      case (value, index) => println(s"index: $index, value: $value")
    }
  }

  "Foreach with sum" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    var sum = 0
    list.foreach(sum += _)
    assert(sum == 15)
  }

  "Map" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    val result = list.map(_ * 2)
    assert(result == List(2, 4, 6, 8, 10))
  }

  "Map2" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    val result = list.map(i => i * 2)
    assert(result == List(2, 4, 6, 8, 10))
  }

  "Map3" should "work" in {
    val list = List(1, 2, 3, 4, 5)
    val result = list.map(i => {
      i * 2
    })
    assert(result == List(2, 4, 6, 8, 10))
  }

  "Range" should "work" in {
    val range = 1 to 10
    assert(range == List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))

    val range2 = 1 until 10
    assert(range2 == List(1, 2, 3, 4, 5, 6, 7, 8, 9))

    val range3 = 1 to 10 by 2
    assert(range3 == List(1, 3, 5, 7, 9))

    val range4 = 1 until 10 by 2
    assert(range4 == List(1, 3, 5, 7, 9))

    val range5 = 10 to 1 by -1 // reverse -1 step because default is 1 so we need to specify -1 to go down
    assert(range5 == List(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
  }


  "Range examples" should "work" in {
    val range = 1 to 10
    val range2 = 1 until 10
    val range3 = 1 to 10 by 2
    val range4 = (1 to 10).toList
    val range5 = (1 to 10).toArray
    val range6 = (1 to 10).toSeq
    val range7 = Array.range(1, 10)
    val range8 = List.range(1, 10)
    val range9 = Seq.range(1, 10)
    val range10 = 'a' to 'z'
    val range11 = 'a' to 'z' by 2
    val range12 = (1 to 10).map(_ * 2)
  }

}
