package user

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class UserTest extends BaseTest {

  "Test" should "XD" in {
    Get("/api/v1/user/test@wp.pl") ~> routes.get ~> check {
      status shouldEqual StatusCodes.NotFound
      responseAs[String] shouldEqual """{"message":"User with email test@wp.pl not found"}"""
    }
  }
}
