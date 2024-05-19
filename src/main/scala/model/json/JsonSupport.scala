package model.json

import akka.http.scaladsl.marshalling._
import akka.http.scaladsl.model.MediaTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest}
import akka.http.scaladsl.unmarshalling._
import akka.stream.Materializer
import com.fasterxml.jackson.databind._
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable, ExecutionContext, Future}
import scala.language.postfixOps
import scala.reflect.ClassTag

trait JsonSupport {

  private val mapper = new ObjectMapper()
  mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
  mapper.registerModule(DefaultScalaModule)

  implicit def JacksonMarshaller: ToEntityMarshaller[AnyRef] = {
    Marshaller.withFixedContentType(`application/json`) {
      case future: Awaitable[AnyRef] => HttpEntity(`application/json`, mapper.writeValueAsString(Await.result(future, 5.seconds)).getBytes("UTF-8"))
      case obj => HttpEntity(`application/json`, mapper.writeValueAsString(obj).getBytes("UTF-8"))
    }
  }

  implicit def JacksonUnmarshaller[T <: AnyRef](implicit c: ClassTag[T]): FromRequestUnmarshaller[T] = {
    new FromRequestUnmarshaller[T] {
      override def apply(request: HttpRequest)(implicit ec: ExecutionContext, materializer: Materializer): Future[T] = {
        request.entity.toStrict(5.seconds).map(_.data.decodeString("UTF-8")).map { str =>
          mapper.readValue(str, c.runtimeClass).asInstanceOf[T]
        }
      }
    }
  }
}