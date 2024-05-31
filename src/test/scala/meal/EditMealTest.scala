package meal

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class EditMealTest extends BaseTest {

  "Edit meal" should "succeed with created 202" in {
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

    val editMealCommand = objectMapper.writeValueAsString(Map(
      "meal_type" -> "lunch",
      "date" -> "12/12/2023"))
    Put(s"/api/v1/meal/${meal_id.get}", editMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Accepted
      responseAs[Map[String, String]].keySet should contain("meal_id")
    }
  }

  "Try to edit meal when not logged in" should "fail with unauthorized 401" in {
    val editMealCommand = objectMapper.writeValueAsString(Map(
      "meal_type" -> "lunch",
      "date" -> "12/12/2023"))
    val expectedResponse = Map("message" -> "Unauthorized")
    Put("/api/v1/meal/1", editMealCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to edit meal with invalid meal type" should "fail with bad request 400" in {
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

    val editMealCommand = objectMapper.writeValueAsString(Map(
      "meal_type" -> "invalid",
      "date" -> "12/12/2023"))
    Put(s"/api/v1/meal/${meal_id.get}", editMealCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
    }
  }
}
