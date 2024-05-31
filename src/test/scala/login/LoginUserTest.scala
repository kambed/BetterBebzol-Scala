package login

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class LoginUserTest extends BaseTest {

  "Login user" should "succeed with ok 200" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password"))
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
    }
  }

  "Try login with incorrect password" should "fail with unauthorized 401" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test2@gmail.com", "password" -> "password2"))
    val expectedResponse = Map("message" -> "Invalid email or password")

    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try login not existing user" should "fail with user not found 404" in {
    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test3@gmail.com", "password" -> "password"))
    val expectedResponse = Map("message" -> "User with email test3@gmail.com not found")

    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }
}
