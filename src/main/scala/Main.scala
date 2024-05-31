import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.{Config, ConfigFactory}
import io.github.cdimascio.dotenv.Dotenv
import model.command.abstracts.Command
import rest.api.RestRoutes
import util.Supervisor

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main extends App {
  private val dotenv = Dotenv.load()
  dotenv.entries().forEach(entry => System.setProperty(entry.getKey, entry.getValue))
  private val config: Config = ConfigFactory.load.resolve
  implicit val system: ActorSystem[Command] = ActorSystem[Command](Supervisor(), "system", config)
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  private val serverHost: String = config.getString("akka.http.server.host")
  private val serverPort: Int = config.getInt("akka.http.server.port")

  system.log.info(s"Starting webserver on $serverHost:$serverPort...")
  private val bindingFuture = Http().newServerAt(serverHost, serverPort).bindFlow(new RestRoutes().allRoutes)
  bindingFuture.onComplete {
    case Success(_) =>
      system.log.info("==========================================================")
      system.log.info(s"| Webserver started on http://$serverHost:$serverPort             |")
      system.log.info(s"| Swagger UI available on http://$serverHost:$serverPort/api-docs |")
      system.log.info("==========================================================")
    case Failure(exception) =>
      system.log.error("Failed to start webserver", exception)
      system.terminate()
  }
}
