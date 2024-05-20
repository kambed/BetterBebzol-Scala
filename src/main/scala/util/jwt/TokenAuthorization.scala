package util.jwt

import akka.http.scaladsl.server.Directive1
import model.domain.User
import pdi.jwt.{Jwt, JwtAlgorithm}
import rest.api.controller.BaseController

object TokenAuthorization extends BaseController {
  private val secretKey = "super_secret_key"

  def generateToken(user: User): String = {
    Jwt.encode(s"""{"id":${user.userId},"email":"${user.email}"}""", secretKey, JwtAlgorithm.HS256)
  }

  def authenticated: Directive1[Map[String, String]] = {
    optionalHeaderValueByName("Authorization").flatMap { tokenFromUser =>
      val jwtToken = tokenFromUser.getOrElse("").split(" ")
      if (jwtToken.length != 2 || jwtToken(0) != "Bearer") {
        completeWith401()
      } else {
        jwtToken(1) match {
          case token if Jwt.isValid(token, secretKey, Seq(JwtAlgorithm.HS256)) =>
            provide(getClaims(token))

          case _ => completeWith403()
        }
      }
    }
  }

  private def getClaims(jwt: String): Map[String, String] = {
    val claimsStringOption = Jwt.decode(jwt, secretKey, Seq(JwtAlgorithm.HS256)).toOption
    if (claimsStringOption.isEmpty) {
      return Map.empty
    }
    mapper.readValue(claimsStringOption.get.content, classOf[Map[String, String]])
  }
}