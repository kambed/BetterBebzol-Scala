package database.connection

import slick.jdbc.MySQLProfile.api._

object MySQLConnection {
  val db = Database.forConfig("mysql")
}