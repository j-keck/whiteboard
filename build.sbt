import sbt.dsl._

name := "whiteboard"

version := "0.0.1"

scalaVersion := "2.11.6"

libraryDependencies ++= {
  val http4sVersion = "0.8.1"
  Seq(
    "org.scala-js" %%% "scalajs-dom" % "0.8.1",
    "com.lihaoyi" %%% "scalatags" % "0.5.2",
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-blazeserver" % http4sVersion,
    "ch.qos.logback" % "logback-classic" % "1.1.3"
  )
}

enablePlugins(ScalaJSPlugin)