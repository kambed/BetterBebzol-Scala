import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import rest.api.RestRoutes

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

@main
def main(): Unit = {
  implicit val system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "my-system")
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(RestRoutes().allRoutes)

  println(s"Server now online. Please navigate to http://localhost:8080/api/healthcheck\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}