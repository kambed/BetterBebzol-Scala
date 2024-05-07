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

  println("Starting webserver...")
  val bindingFuture = Http().newServerAt("localhost", 8080).bind(RestRoutes().allRoutes)
  println(s"Server now online on http://localhost:8080\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}