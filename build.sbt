name := "JodaProject"

version := "0.1"

scalaVersion := "2.11.11"

lazy val root = (project in file(".")).enablePlugins()

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.8.0",
  "com.typesafe" % "config" % "1.2.1",
  "org.scala-tools.time" % "time_2.8.0" % "0.2",
  "org.apache.directory.api" % "api-all" % "1.0.0",
  "com.jsuereth" %% "scala-arm" % "2.0"
)