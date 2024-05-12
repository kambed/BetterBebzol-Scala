import akka.actor.typed.ActorSystem
import akka.actor.typed.javadsl.Behaviors
import akka.http.scaladsl.Http
import rest.api.RestRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

@main
def main(): Unit = {
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
 
  system.log.info("Starting webserver...")
  val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(RestRoutes().allRoutes)
  bindingFuture.onComplete {
    case Success(binding) =>
      system.log.info("Webserver started on http://localhost:8080!")
    case Failure(exception) =>
      system.log.error("Failed to start webserver", exception)
      system.terminate()
  }
}