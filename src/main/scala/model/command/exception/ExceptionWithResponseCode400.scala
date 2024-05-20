package model.command.exception

case class ExceptionWithResponseCode400(message: String) extends Exception(message)
