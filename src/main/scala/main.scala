import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import rest.api.RestRoutes
import util.Supervisor

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

@main
def main(): Unit = {
  implicit val system: ActorSystem[Nothing] = ActorSystem[Nothing](Supervisor(), "system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  system.log.info("Starting webserver...")
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(RestRoutes().allRoutes)
  system.log.info("Webserver started on http://localhost:8080!")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}