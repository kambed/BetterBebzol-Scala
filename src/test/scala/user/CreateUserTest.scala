package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest
import model.domain.enums.{UserActivity, UserGoal, UserSex}
import model.dto.UserDto

class CreateUserTest extends BaseTest {

  "Create user" should "succeed with created 201 and correct data" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com",
      "password" -> "password",
      "sex" -> "male",
      "age" -> 23,
      "height" -> 180,
      "weight" -> 80,
      "how_active" -> "active",
      "goal" -> "lose_weight"))
    val expectedResponse = UserDto(email = "test@gmail.com", sex = Some("male"), age = Some(23),
      height = Some(180), weight = Some(80), howActive = Some("active"), goal = Some("lose_weight"))

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }
  }

  "Create user without optional parameters" should "succeed with created 201 and correct data" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test2@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }
  }

  "Try to create user with duplicate email parameter" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map("email" -> "test100@gmail.com", "password" -> "password"))
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
    }

    val expectedError = Map("message" -> "Duplicate entry 'test100@gmail.com' for key 'email'")
    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]] shouldEqual expectedError
    }
  }

  "Try to create user with missing password parameter" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map("email" -> "test3@gmail.com"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Password cannot be null"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with empty email" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map("email" -> "", "password" -> "password"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Email cannot be empty"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with missing email parameter" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map("password" -> "password"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Email cannot be null"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with empty password" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map("email" -> "Try to test3@gmail.com", "password" -> ""))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Password cannot be empty"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid sex" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "sex" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Sex can be one of: " + UserSex.values.map(v => v.toString)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid age" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "age" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Age has to be within a [0, 150] range"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    val createUserCommand2 = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "age" -> 151))

    Post("/api/v1/user", createUserCommand2) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid height" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "height" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Height has to be within a [0, 300] range"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    val createUserCommand2 = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "height" -> 301))

    Post("/api/v1/user", createUserCommand2) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid weight" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "weight" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Weight has to be within a [0, 500] range"

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    val createUserCommand2 = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "weight" -> 501))

    Post("/api/v1/user", createUserCommand2) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid howActive" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "how_active" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: How active can be one of: " + UserActivity.values.map(v => v.toString)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to create user with invalid goal" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password", "goal" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.CreateUserCommand`, problem: requirement failed: Goal can be one of: " + UserGoal.values.map(v => v.toString)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }
}