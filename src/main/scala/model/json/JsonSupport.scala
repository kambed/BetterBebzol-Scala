package model.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import model.command.*
import model.dto.*
import spray.json.DefaultJsonProtocol.*
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  //Product
  implicit val productDtoFormat: RootJsonFormat[ProductDto] = jsonFormat3(ProductDto.apply)
  implicit val createProductCommandFormat: RootJsonFormat[CreateProductCommand] = jsonFormat2(CreateProductCommand.apply)
  implicit val productDtoListFormat: RootJsonFormat[List[ProductDto]] = listFormat[ProductDto]
}