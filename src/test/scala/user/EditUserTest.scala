package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest
import model.domain.enums.{UserActivity, UserGoal, UserSex}
import model.dto.UserDto

class EditUserTest extends BaseTest {

  "Edit user" should "succeed with created 201 and correct data" in {
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

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test@gmail.com",
      "sex" -> "female",
      "age" -> 25,
      "height" -> 169,
      "weight" -> 45,
      "how_active" -> "moderate",
      "goal" -> "gain_weight"))
    val expectedResponse2 = UserDto(email = "test@gmail.com", sex = Some("female"), age = Some(25),
      height = Some(169), weight = Some(45), howActive = Some("moderate"), goal = Some("gain_weight"))
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserDto] shouldEqual expectedResponse2
    }
  }

  "Try to edit user when not logged in" should "fail with unauthorized 401" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test2@gmail.com", "password" -> "password"))
    val expectedResponse = UserDto(email = "test2@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("sex" -> "male"))
    val expectedError = Map("message" -> "Unauthorized")
    Put("/api/v1/user", editUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedError
    }
  }

  "Try to edit other user than is logged in" should "fail with forbidden 403" in {
    var createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test3@gmail.com", "password" -> "password"))
    var expectedResponse = UserDto(email = "test3@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test4@gmail.com", "password" -> "password"))
    expectedResponse = UserDto(email = "test4@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test3@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test4@gmail.com", "sex" -> "male"))
    val expectedError = Map("message" -> "Forbidden: User can only edit their own account")
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Forbidden
      responseAs[Map[String, String]] shouldEqual expectedError
    }
  }

  "Try to edit user without email" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test5@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test5@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test5@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("sex" -> "male"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Email cannot be null"
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with empty email" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test6@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test6@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test6@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("email" -> "", "sex" -> "male"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Email cannot be empty"
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid sex" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test7@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test7@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test7@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test7@gmail.com", "sex" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Sex can be one of: " + UserSex.values.map(v => v.toString)
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid age" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test8@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test8@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test8@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    var editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test8@gmail.com", "age" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Age has to be within a [0, 150] range"
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test8@gmail.com", "age" -> 151))
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid height" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test9@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test9@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test9@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    var editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test9@gmail.com", "height" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Height has to be within a [0, 300] range"
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test9@gmail.com", "height" -> 301))
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid weight" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test10@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test10@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test10@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    var editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test10@gmail.com", "weight" -> -1))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Weight has to be within a [0, 500] range"
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }

    editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test10@gmail.com", "weight" -> 501))
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid howActive" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test11@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test11@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test11@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test11@gmail.com", "how_active" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: How active can be one of: " + UserActivity.values.map(v => v.toString)
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }

  "Try to edit user with invalid goal" should "fail with bad request 400" in {
    val createUserCommand = objectMapper.writeValueAsString(Map(
      "email" -> "test12@gmail.com",
      "password" -> "password"))
    val expectedResponse = UserDto(email = "test12@gmail.com", sex = None, age = None,
      height = None, weight = None, howActive = None, goal = None)

    Post("/api/v1/user", createUserCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[UserDto] shouldEqual expectedResponse
    }

    val loginCommand = objectMapper.writeValueAsString(Map("email" -> "test12@gmail.com", "password" -> "password"))
    var token: Option[String] = None
    Post("/api/v1/user/login", loginCommand) ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("token")
      token = responseAs[Map[String, String]].get("token")
    }

    val editUserCommand = objectMapper.writeValueAsString(Map("email" -> "test12@gmail.com", "goal" -> "invalid"))
    val expectedError = "The request content was malformed: Cannot construct instance of `model.command.user.EditUserCommand`, problem: requirement failed: Goal can be one of: " + UserGoal.values.map(v => v.toString)
    Put("/api/v1/user", editUserCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
      responseAs[Map[String, String]].keySet should contain("message")
      responseAs[Map[String, String]].getOrElse("message", "") should startWith(expectedError)
    }
  }
}