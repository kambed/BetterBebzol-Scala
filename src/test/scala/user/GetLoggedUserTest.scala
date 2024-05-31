package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest
import model.dto.UserDto

class GetLoggedUserTest extends BaseTest {

  "Get logged user" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com", "password" -> "password"))
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

    val expectedResponse = UserDto(email = "test@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)
    Get("/api/v1/user") ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserDto] shouldEqual expectedResponse
    }
  }

  "Try to get user when not logged in" should "fail with unauthorized 401" in {
    val expectedResponse = Map("message" -> "Unauthorized")
    Get("/api/v1/user") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }
}
