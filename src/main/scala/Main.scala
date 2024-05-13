import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import model.command.abstracts.Command
import rest.api.RestRoutes
import util.Supervisor

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {
  implicit val system: ActorSystem[Command] = ActorSystem[Command](Supervisor(), "system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  system.log.info("Starting webserver...")
  private val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(new RestRoutes().allRoutes)
  bindingFuture.onComplete {
    case Success(_) =>
      system.log.info("==========================================================")
      system.log.info("| Webserver started on http://localhost:8080             |")
      system.log.info("| Swagger UI available on http://localhost:8080/api-docs |")
      system.log.info("==========================================================")
    case Failure(exception) =>
      system.log.error("Failed to start webserver", exception)
      system.terminate()
  }
}
