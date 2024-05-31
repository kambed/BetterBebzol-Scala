package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class UserTest extends BaseTest {

  "Get user" should "fail with user not found 404" in {
    val expectedResponse = objectMapper.writeValueAsString(Map("message" -> "User with email test@wp.pl not found"))
    Get("/api/v1/user/test@wp.pl") ~> routes.get ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual expectedResponse
    }
  }
}
