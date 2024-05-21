package model.command.exception

case class ExceptionWithResponseCode401(message: String) extends Exception(message)
