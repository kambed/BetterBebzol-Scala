package database

import slick.jdbc.MySQLProfile.api._

object MySQLConnection {
  var db = Database.forConfig("mysql")
}