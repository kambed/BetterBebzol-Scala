package model.command.exception

case class ExceptionWithResponseCode404(message: String) extends Exception(message)
