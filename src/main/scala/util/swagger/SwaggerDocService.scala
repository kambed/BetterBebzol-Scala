package util.swagger

import akka.http.scaladsl.model.StatusCodes.TemporaryRedirect
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import rest.api.controller.meal.CreateMealController
import rest.api.controller.product.{CreateProductController, ListAllProductsController}
import rest.api.controller.user.{CreateUserController, GetLoggedUserController, GetUserController, LoginUserController}

object SwaggerDocService extends SwaggerHttpService {
  private val userControllers = Set(classOf[CreateUserController], classOf[GetUserController], classOf[GetLoggedUserController], classOf[LoginUserController])
  private val productControllers = Set(classOf[CreateProductController], classOf[ListAllProductsController])
  private val mealControllers = Set(classOf[CreateMealController])
  override val apiClasses: Set[Class[_]] = userControllers ++ productControllers ++ mealControllers
  override val host = "localhost:8080"
  override val info: Info = Info(version = "1.0")
  override val securitySchemes: Map[String, SecurityScheme] =
    Map("bearerAuth" -> new SecurityScheme().`type`(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT"))

  private val swaggerUiRoute = {
    pathPrefix(apiDocsPath) {
      val pathInit = if (apiDocsPath.endsWith("/")) apiDocsPath.substring(0, apiDocsPath.length - 1) else apiDocsPath
      redirect(s"https://petstore.swagger.io/?url=http://localhost:8080/$pathInit/swagger.json", TemporaryRedirect)
    }
  }
  override val routes: Route = super.routes ~ swaggerUiRoute
}