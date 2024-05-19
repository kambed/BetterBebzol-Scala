package util.swagger

import akka.http.scaladsl.model.StatusCodes.TemporaryRedirect
import akka.http.scaladsl.server.Route
import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import model.command.GetUserCommand
import rest.api.controller.user.{CreateUserController, GetUserController}

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(classOf[CreateUserController], classOf[GetUserController])
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