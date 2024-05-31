package product

import akka.http.scaladsl.model.StatusCodes
import base.BaseTest

class EditProductTest extends BaseTest {

  "Edit product" should "succeed with ok 200" in {
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

    val createProductCommand = objectMapper.writeValueAsString(Map(
      "product_name" -> "product1",
      "calories" -> 100.0,
      "protein" -> 10.0,
      "carbohydrates" -> 20.0,
      "fat" -> 5.0,
      "quantity" -> 10))
    var product_id: Option[String] = None
    Post(s"/api/v1/product/${meal_id.get}", createProductCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("product_id")
      product_id = responseAs[Map[String, String]].get("product_id")
    }

    val editProductCommand = objectMapper.writeValueAsString(Map(
      "product_name" -> "product2",
      "calories" -> 200.0,
      "protein" -> 20.0,
      "carbohydrates" -> 40.0,
      "fat" -> 10.0,
      "quantity" -> 20))

    Put(s"/api/v1/product/${product_id.get}", editProductCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.OK
      responseAs[Map[String, String]].keySet should contain("product_id")
    }
  }

  "Try to edit product when not logged in" should "fail with unauthorized 401" in {
    val expectedResponse = Map("message" -> "Unauthorized")
    Put("/api/v1/product/1", "") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Unauthorized
      responseAs[Map[String, String]] shouldEqual expectedResponse
    }
  }

  "Try to edit product with invalid data" should "fail with bad request 400" in {
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

    val createProductCommand = objectMapper.writeValueAsString(Map(
      "product_name" -> "product1",
      "calories" -> 100.0,
      "protein" -> 10.0,
      "carbohydrates" -> 20.0,
      "fat" -> 5.0,
      "quantity" -> 10))
    var product_id: Option[String] = None
    Post(s"/api/v1/product/${meal_id.get}", createProductCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.Created
      responseAs[Map[String, String]].keySet should contain("product_id")
      product_id = responseAs[Map[String, String]].get("product_id")
    }

    val editProductCommand = objectMapper.writeValueAsString(Map(
      "product_name" -> "product2",
      "fat" -> 10.0,
      "quantity" -> 20))

    Put(s"/api/v1/product/${product_id.get}", editProductCommand) ~> addHeader("Authorization", s"Bearer ${token.get}") ~> routes.get ~> check {
      status shouldEqual StatusCodes.BadRequest
    }
  }
}
