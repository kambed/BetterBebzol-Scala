package base

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

trait JsonTestSupport {

  val mapper = new ObjectMapper()
  mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
  mapper.registerModule(DefaultScalaModule)

  implicit def JacksonResponseUnmarshaller[T <: AnyRef](implicit c: ClassTag[T]): FromResponseUnmarshaller[T] = {
    new FromResponseUnmarshaller[T] {
      override def apply(response: HttpResponse)(implicit ec: ExecutionContext, materializer: Materializer): Future[T] = {
        Unmarshal(response.entity).to[String].map { str =>
          mapper.readValue(str, c.runtimeClass).asInstanceOf[T]
        }
      }
    }
  }
}