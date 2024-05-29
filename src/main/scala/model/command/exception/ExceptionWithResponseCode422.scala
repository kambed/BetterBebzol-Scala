package model.command.exception

case class ExceptionWithResponseCode422(message: String) extends Exception(message)
