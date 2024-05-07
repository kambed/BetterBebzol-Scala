package model.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import model.command.*
import model.dto.*
import model.domain.*
import spray.json.DefaultJsonProtocol.*
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  //Product
  implicit val productFormat: RootJsonFormat[Product] = jsonFormat3(Product.apply)
  implicit val productDtoFormat: RootJsonFormat[ProductDto] = jsonFormat3(ProductDto.apply)
  implicit val createProductCommandFormat: RootJsonFormat[CreateProductCommand] = jsonFormat2(CreateProductCommand.apply)
}