package meal

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class GetAllUserMealsTest extends BaseTest {

  "Get all user meals" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test1@gmail.com",
      "password" -> "password",
      "sex" -> "male",
      "age" -> 23,
      "height" -> 180,
      "weight" -> 80,
      "how_active" -> "active",
      "goal" -> "lose_weight"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test1@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val createMealCommand = objectMapper.writeValueAsString(Map(
      "meal_type" -> "breakfast",
      "date" -> "12/12/2023"))
    var meal_idList: List[String] = List()
    Post("/api/v1/meal", createMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("meal_id")
      meal_idList = meal_idList :+ responseAs[Map[String, String]].get("meal_id").get
    }
    Post("/api/v1/meal", createMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("meal_id")
      meal_idList = meal_idList :+ responseAs[Map[String, String]].get("meal_id").get
    }

    Get("/api/v1/meal/all") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Map[String, String]]].size shouldEqual 2
    }
  }

  "Try to get all user meals when not logged in" should "fail with unauthorized 401" in {
    val expectedResponse = Map("message" -> "Unauthorized")
    Get("/api/v1/meal/all") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to get the user's reinforcements when the user has not added any" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com",
      "password" -> "password",
      "sex" -> "male",
      "age" -> 23,
      "height" -> 180,
      "weight" -> 80,
      "how_active" -> "active",
      "goal" -> "lose_weight"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test2@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    Get("/api/v1/meal/all") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[List[Map[String, String]]].size shouldEqual 0
    }
  }
}
