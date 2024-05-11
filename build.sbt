ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

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
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,
  "mysql" % "mysql-connector-java" % MySqlConnectorVersion,
  "com.typesafe.slick" %% "slick" % SlickVersion
)
