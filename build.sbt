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
val JacksonVersion = "2.17.1"
val MySqlConnectorVersion = "8.0.33"
val SlickVersion = "3.5.1"
libraryDependencies ++= Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % LogbackVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "jakarta.ws.rs" % "jakarta.ws.rs-api" % "4.0.0",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % "2.11.0",
  "io.swagger.core.v3" % "swagger-jaxrs2-jakarta" % "2.2.21",
  "ch.megard" %% "akka-http-cors" % "1.2.0",
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "mysql" % "mysql-connector-java" % MySqlConnectorVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion
)
