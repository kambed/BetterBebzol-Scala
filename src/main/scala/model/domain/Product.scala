package model.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport.*

case class Product(id: Long, name: String, calories: Int)