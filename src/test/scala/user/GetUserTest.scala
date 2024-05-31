package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest
import model.dto.UserDto

class GetUserTest extends BaseTest {

  "Get user by email" should "fail with user not found 404" in {
    val expectedResponse = Map("message" -> "User with email test@gmail.com not found")
    Get("/api/v1/user/test@gmail.com") ~> routes.get ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Get user by email" should "succeed with user found 200" in {
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

    val expectedResponse = UserDto(email = "test@gmail.com", sex = Some("male"), age = Some(23),
      height = Some(180), weight = Some(80), howActive = Some("active"), goal = Some("lose_weight"))
    Get("/api/v1/user/test@gmail.com") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[UserDto] shouldEqual expectedResponse
    }
  }

}
