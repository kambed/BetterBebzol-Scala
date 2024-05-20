package p.lodz.pl

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers._

class TopicOneExampleTests extends AnyFlatSpec {

  "An array" should "initialize with 3 elements" in {
    val array: Array[Int] = Array(1, 2, 3)
    val array2 = Array(1, 2, 3)

    array should have size 3
    array should contain theSameElementsInOrderAs Array(1, 2, 3)
    array2 should have size 3
    array2 should contain theSameElementsInOrderAs Array(1, 2, 3)
  }

  "Array first element" should "be 1" in {
    val array: Array[Int] = Array(1, 2, 3)
    array(0) should be (1)
  }

  "Array size" should "be correct" in {
    val array: Array[Int] = Array(1, 2, 3)
    array.size should be (3)

    val array2 = Array.emptyIntArray
    array2.size should be (0)
  }

  "Array" should "be reversed" in {
    val array: Array[Int] = Array(1, 2, 3)
    val reversedArray = array.reverse

    reversedArray should have size 3
    reversedArray should contain theSameElementsInOrderAs Array(3, 2, 1)
  }

  "Array new element" should "be added" in {
    val array: Array[Int] = Array(1, 2, 3)
    val newArray = array :+ 4

    newArray should have size 4
    newArray should contain theSameElementsInOrderAs Array(1, 2, 3, 4)
  }

  "Array replace element" should "be replaced" in {
    val array: Array[Int] = Array(1, 2, 3)
    array(0) = 4

    array should have size 3
    array should contain theSameElementsInOrderAs Array(4, 2, 3)
  }

  "Array" should "be sorted" in {
    val array: Array[Int] = Array(3, 1, 2)
    array.sorted
    val sortedArray = array.sorted // or array.sortWith(_ < _)
    val sortedArray2 = array.sortWith(_ > _)

    sortedArray should contain theSameElementsInOrderAs Array(1, 2, 3)
    sortedArray2 should contain theSameElementsInOrderAs Array(3, 2, 1)
  }

  "All elements" should "be multiplied by 2" in {
    val array: Array[Int] = Array(1, 2, 3)
    val multipliedArray = array.map(_ * 2)

    multipliedArray should have size 3
    multipliedArray should contain theSameElementsInOrderAs Array(2, 4, 6)
  }

  "Array" should "have 2 dimensions" in {
    val array: Array[Array[Int]] = Array(Array(1, 2), Array(3, 4))
    val array2 = Array.ofDim[Int](2, 3) // Array.fill(2, 3)(0)


    array(0) should contain theSameElementsInOrderAs Array(1, 2)
    array(1) should contain theSameElementsInOrderAs Array(3, 4)
    array2(0) should contain theSameElementsInOrderAs Array(0, 0, 0)
    array2(1) should contain theSameElementsInOrderAs Array(0, 0, 0)
  }

  "Sequence" should "be created" in {
    val sequence: Seq[Int] = Seq(1, 2, 3) // or (1 to 3).toSeq
    val sequence2 = Seq(1, 2, 3) // or Seq.apply(1, 2, 3)

    sequence should have size 3
    sequence should contain theSameElementsInOrderAs Seq(1, 2, 3)
    sequence2 should have size 3
    sequence2 should contain theSameElementsInOrderAs Seq(1, 2, 3)
  }

  "Sequence" should "add new element" in {
    val sequence: Seq[Int] = Seq(1, 2, 3)
    val newSequence = sequence :+ 4

    newSequence should have size 4
    newSequence should contain theSameElementsInOrderAs Seq(1, 2, 3, 4)
  }

  "Two sequences" should "be concatenated" in {
    val sequence: Seq[Int] = Seq(1, 2, 3)
    val sequence2: Seq[Int] = Seq(4, 5, 6)
    val concatenatedSequence = sequence ++ sequence2

    concatenatedSequence should have size 6
    concatenatedSequence should contain theSameElementsInOrderAs Seq(1, 2, 3, 4, 5, 6)
  }

  "Sequence" should "be filtered" in {
    val sequence: Seq[Int] = Seq(1, 2, 3)
    val filteredSequence = sequence.filter(_ > 1)

    filteredSequence should have size 2
    filteredSequence should contain theSameElementsInOrderAs Seq(2, 3)
  }

  "Sequence" should "add element" in {
    val sequence: Seq[Int] = Seq(1, 2, 3)
    sequence.appended(4)

    sequence should have size 4
  }

  "List" should "be created" in {
    val list: List[Int] = List(1, 2, 3)
    val list2 = List(1, 2, 3)
    val list3 = 1 :: 2 :: 3 :: Nil
    val list4 = List.range(1, 4)

    list should contain theSameElementsInOrderAs List(1, 2, 3)
    list2 should contain theSameElementsInOrderAs List(1, 2, 3)
    list3 should contain theSameElementsInOrderAs List(1, 2, 3)
    list4 should contain theSameElementsInOrderAs List(1, 2, 3)
  }

  "List" should "add element in front" in {
    val list: List[Int] = List(1, 2, 3)
    val newList = 0 :: list

    newList should contain theSameElementsInOrderAs List(0, 1, 2, 3)
  }

  "Two lists" should "be concatenated" in {
    val list: List[Int] = List(1, 2, 3)
    val list2: List[Int] = List(4, 5, 6)
    val concatenatedList = list ::: list2

    concatenatedList should contain theSameElementsInOrderAs List(1, 2, 3, 4, 5, 6)
  }

  "List" should "be iterated" in {
    val list: List[Int] = List(1, 2, 3)
    var result = ""
    list.foreach(i => result += i)

    result should be ("123")
  }

  "List" should "be change while iterating" in {
    val list: List[Int] = List.range(1, 20)
    val result = list.takeWhile(_ < 10)

    result should contain theSameElementsInOrderAs List.range(1, 10)
  }

  "Tuple" should "be created" in {
    val tuple: (Int, String) = (1, "one")
    val tuple2 = (1, "one")

    tuple should be (1, "one")
    tuple2 should be (1, "one")
  }

  "Tuple elements" should "be accessed" in {
    val tuple: (Int, String) = (1, "one")
    val number = tuple._1
    val text = tuple._2

    number should be (1)
    text should be ("one")

    val (number2, text2) = tuple

    number2 should be (1)
    text2 should be ("one")
  }

  "Tuple" should "be zipped" in {
    val tuple: (Int, String) = (1, "one")
    val tuple2: (Int, String) = (2, "two")
    val zippedTuple = tuple._1 -> tuple2._2

    zippedTuple should be (1 -> "two")
  }

  "Tuple" should "be iterated" in {
    val tuple: (Int, String) = (1, "one")
    var result = ""
    tuple.productIterator.foreach(i => result += i)

    result should be ("1one")
  }

  "Array" should "add element" in {
    val arr = Array(1, 2, 3, 4, 5)
    val newArr = arr.map(_ * 2).filter(_ % 3 == 0)
    println(newArr.mkString(", "))
  }


}