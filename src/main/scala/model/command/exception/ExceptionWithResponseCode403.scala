package model.command.exception

case class ExceptionWithResponseCode403(message: String) extends Exception(message)
