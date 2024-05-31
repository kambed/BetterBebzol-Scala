package base

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{ContainerDef, JdbcDatabaseContainer, MySQLContainer}
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.typesafe.config.{Config, ConfigFactory}
import database.MySQLConnection
import io.github.cdimascio.dotenv.Dotenv
import model.command.abstracts.Command
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.testcontainers.containers
import org.testcontainers.utility.DockerImageName
import rest.api.RestRoutes
import slick.jdbc.MySQLProfile.api._
import util.Supervisor

class BaseTest extends AnyFlatSpec with BeforeAndAfterAll with TestContainerForAll with ScalatestRouteTest with Matchers with JsonTestSupport {

  override val containerDef: ContainerDef = MySQLContainer.Def(
    dockerImageName = DockerImageName.parse("mysql:5.7"),
    databaseName = "db",
    username = "user",
    password = "password",
    commonJdbcParams = JdbcDatabaseContainer.CommonParams().copy(
      initScriptPath = Some("init.sql")
    )
  )
  protected val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)
  protected var container: Option[containers.MySQLContainer[_]] = None
  protected var routes: Option[Route] = None

  override def afterContainersStart(containers: containerDef.Container): Unit = {
    super.afterContainersStart(containers)
    container = Some(containers.asInstanceOf[MySQLContainer].container)
  }

  override protected def beforeAll(): Unit = {
    val dotenv = Dotenv.load()
    dotenv.entries().forEach(entry => System.setProperty(entry.getKey, entry.getValue))
    System.setProperty("MYSQL_PORT", container.get.getMappedPort(3306).toString)
    ConfigFactory.invalidateCaches()
    val config: Config = ConfigFactory.load.resolve
    MySQLConnection.db = Database.forConfig("mysql", config)
    implicit val testSystem: ActorSystem[Command] = ActorSystem[Command](Supervisor(), "system", config)
    routes = Some(new RestRoutes().allRoutes)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    container.get.stop()
  }
}