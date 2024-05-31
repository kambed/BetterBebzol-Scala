package login

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class EditUserPasswordTest extends BaseTest {

  "Edit user password" should "succeed with no content 204" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    var loginCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password2"))
    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.NoContent
    }

    loginCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password2"))
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
    }
  }

  "Try to edit password when not logged in" should "fail with unauthorized 401" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "test2@gmail.com", "password" -> "password2"))
    val expectedResponse = Map("message" -> "Unauthorized")
    Put("/api/v1/user/login", editUserPasswordCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to edit password when logged in on different user" should "fail with forbidden 403" in {
    var createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }
    createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test4@gmail.com",
      "password" -> "password"))
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

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "test4@gmail.com", "password" -> "password2"))
    val expectedResponse = Map("message" -> "Forbidden: User can only edit their own account")
    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Forbidden
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to edit user password with missing password parameter" should "succeed with no content 204" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test5@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test5@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "test5@gmail.com"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.EditUserPasswordCommand`, problem: requirement failed: Password cannot be null"

    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user password with empty password parameter" should "succeed with no content 204" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test6@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test6@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "test6@gmail.com", "password" -> ""))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.EditUserPasswordCommand`, problem: requirement failed: Password cannot be empty"

    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }
  "Try to edit user password with missing email parameter" should "succeed with no content 204" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test7@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test7@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("password" -> "password2"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.EditUserPasswordCommand`, problem: requirement failed: Email cannot be null"

    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user password with empty email parameter" should "succeed with no content 204" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test8@gmail.com",
      "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test8@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserPasswordCommand = objectMapper.writeValueAsString(Map("email" -> "", "password" -> "password2"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.EditUserPasswordCommand`, problem: requirement failed: Email cannot be empty"

    Put("/api/v1/user/login", editUserPasswordCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }
}
