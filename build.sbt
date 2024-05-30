ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "BetterBebzolAkka"
  )

resolvers += "Akka library repository".at("https://repo.akka.io/maven")

val ScalaLoggingVersion = "3.9.5"
val LogbackVersion = "1.5.6"
val AkkaVersion = "2.9.2"
val AkkaHttpVersion = "10.6.2"
val SwaggerAkkaHttpVersion = "2.11.0"
val AkkaCorsVersion = "1.2.0"
val JakartaWsRsVersion = "4.0.0"
val SwaggerJaxrs2JakartaVersion = "2.2.21"
val JacksonVersion = "2.17.1"
val MySqlConnectorVersion = "8.0.33"
val SlickVersion = "3.5.1"
val ConfigVersion = "0.6.1"
val ScalaTestVersion = "3.2.18"
val TestcontainersScalaVersion = "0.41.3"
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerAkkaHttpVersion,
  "ch.megard" %% "akka-http-cors" % AkkaCorsVersion,
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % JakartaWsRsVersion,
  "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % SwaggerJaxrs2JakartaVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "mysql" % "mysql-connector-java" % MySqlConnectorVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion,
  "com.github.jwt-scala" %% "jwt-core" % "10.0.1",
  "at.favre.lib" % "bcrypt" % "0.10.2",
  "com.github.kxbmap" %% "configs" % ConfigVersion,
  "io.github.cdimascio" % "java-dotenv" % "5.2.2",
  "com.typesafe.akka" %% "akka-testkit" % AkkaVersion % "test",
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % "test",
  "org.scalatest" %% "scalatest" % ScalaTestVersion % "test",
  "com.dimafeng" %% "testcontainers-scala-scalatest" % TestcontainersScalaVersion % "test",
  "com.dimafeng" %% "testcontainers-scala-mysql" % TestcontainersScalaVersion % "test"
)
