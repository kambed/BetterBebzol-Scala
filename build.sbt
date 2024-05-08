ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.3"

lazy val root = (project in file("."))
  .settings(
    name := "workshop",
    idePackagePrefix := Some("p.lodz.pl")
  )

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.18" % "test",
  "ch.qos.logback" % "logback-classic" % "1.5.6"
)