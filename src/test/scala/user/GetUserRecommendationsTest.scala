package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest
import model.dto.{UserDto, UserProfileDto}

class GetUserRecommendationsTest extends BaseTest {

  "Get male user recommendations" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com",
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

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val expectedResponse = UserProfileDto(email = "test@gmail.com", goal = "lose_weight", calories = 2797, protein = 129.6, fat = 60.0, carbohydrates = 384.59)
    Get("/api/v1/user/recommendations") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserProfileDto] shouldEqual expectedResponse
    }
  }

  "Get female user recommendations" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com",
      "password" -> "password",
      "sex" -> "female",
      "age" -> 21,
      "height" -> 165,
      "weight" -> 52,
      "how_active" -> "moderate",
      "goal" -> "gain_weight"))
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

    val expectedResponse = UserProfileDto(email = "test2@gmail.com", goal = "gain_weight", calories = 2607, protein = 80.08, fat = 65.0, carbohydrates = 358.46)
    Get("/api/v1/user/recommendations") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserProfileDto] shouldEqual expectedResponse
    }
  }

  "Get user recommendations without goal" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com",
      "password" -> "password",
      "sex" -> "male",
      "age" -> 23,
      "height" -> 180,
      "weight" -> 68,
      "how_active" -> "little"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test3@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val expectedResponse = UserProfileDto(email = "test3@gmail.com", goal = "maintain_weight", calories = 2401, protein = 81.6, fat = 68.0, carbohydrates = 330.14)
    Get("/api/v1/user/recommendations") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserProfileDto] shouldEqual expectedResponse
    }
  }

  "Get user recommendations without optional profile parameters" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test4@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test4@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val expectedError = Map("message" -> "User profile is incomplete, recommendations cannot be calculated")
    Get("/api/v1/user/recommendations") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]] shouldEqual expectedError
    }
  }

  "Get user recommendations when not logged in" should "fail with unauthorized 401" in {
    val expectedError = Map("message" -> "Unauthorized")
    Get("/api/v1/user/recommendations") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedError
    }
  }
}
