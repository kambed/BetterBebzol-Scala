package meal

import akka.http.scaladsl.model.{StatusCodes, Uri}
import base.BaseTest

class GetMealByDateTest extends BaseTest {

  "Get meal by date" should "succeed with ok 200" in {
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
    var meal_id: Option[String] = None
    Post("/api/v1/meal", createMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("meal_id")
      meal_id = responseAs[Map[String, String]].get("meal_id")
    }

    val requestUri = Uri(s"/api/v1/meal/date").withQuery(Uri.Query("date" -> "12/12/2023"))
    Get(requestUri) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      val response = responseAs[Seq[Map[String, String]]]
      response should not be empty
    }
  }

  "Try to get meal by date when not logged in" should "fail with unauthorized 401" in {
    val requestUri = Uri(s"/api/v1/meal/date").withQuery(Uri.Query("date" -> "12/12/2023"))
    val expectedResponse = Map("message" -> "Unauthorized")
    Get(requestUri) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to get meal by date with invalid date" should "fail with bad request 404" in {
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

    val createMealCommand = objectMapper.writeValueAsString(Map(
      "meal_type" -> "breakfast",
      "date" -> "12/12/2023"))
    var meal_id: Option[String] = None
    Post("/api/v1/meal", createMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("meal_id")
      meal_id = responseAs[Map[String, String]].get("meal_id")
    }

    val requestUri = Uri(s"/api/v1/meal/date").withQuery(Uri.Query("date" -> "12212/2023"))
    Get(requestUri) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.NotFound
    }
  }
}
