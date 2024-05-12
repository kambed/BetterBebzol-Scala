package database

import slick.jdbc.MySQLProfile.api.*

object MySQLConnection {
  val db = Database.forConfig("mysql")
}