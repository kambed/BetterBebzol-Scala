package util.swagger

import akka.http.scaladsl.model.StatusCodes.TemporaryRedirect
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.security.{SecurityRequirement, SecurityScheme}
import rest.api.controller.meal.{CreateMealController, EditMealController}
import rest.api.controller.login.{EditUserPasswordController, LoginUserController}
import rest.api.controller.meal.CreateMealController
import rest.api.controller.product.{CreateProductController, ListAllProductsController}
import rest.api.controller.user._

object SwaggerDocService extends SwaggerHttpService {
  private val userControllers = Set(classOf[CreateUserController], classOf[EditUserController], classOf[GetUserController],
    classOf[GetLoggedUserController], classOf[LoginUserController], classOf[EditUserPasswordController], classOf[GetUserRecommendationsController])
  private val productControllers = Set(classOf[CreateProductController], classOf[ListAllProductsController])
  private val mealControllers = Set(classOf[CreateMealController], classOf[EditMealController])
  override val apiClasses: Set[Class[_]] = userControllers ++ productControllers ++ mealControllers
  override val host = "localhost:8080"
  override val info: Info = Info(version = "1.0")
  private final val bearer = new SecurityScheme().name("Bearer Security").description("Bearer Token based")
  bearer.setType(SecurityScheme.Type.HTTP)
  bearer.setScheme("Bearer")
  bearer.setBearerFormat("JWT")
  bearer.setScheme("Bearer")
  override val securitySchemes: Map[String, SecurityScheme] = Map("bearerAuth" -> bearer)
  override val security: List[SecurityRequirement] = List(new SecurityRequirement().addList("bearerAuth"))

  private val swaggerUiRoute = {
    pathPrefix(apiDocsPath) {
      val pathInit = if (apiDocsPath.endsWith("/")) apiDocsPath.substring(0, apiDocsPath.length - 1) else apiDocsPath
      redirect(s"https://petstore.swagger.io/?url=http://localhost:8080/$pathInit/swagger.json", TemporaryRedirect)
    }
  }
  override val routes: Route = super.routes ~ swaggerUiRoute
}