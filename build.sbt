import sbt.dsl._

val http4sVersion = "0.8.1"

lazy val root = project.in(file(".")).
  aggregate(wbJS, wbJVM)


lazy val wb = crossProject.in(file(".")).
  settings(
    name := "whiteboard",
    version := "0.0.1",
    scalaVersion := "2.11.6"
  ).jvmSettings(
  libraryDependencies ++=
    Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blazeserver" % http4sVersion,
      "ch.qos.logback" % "logback-classic" % "1.1.3",
      "io.argonaut" %% "argonaut" % "6.0.4"
    )
).jsSettings(
  libraryDependencies ++=
    Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.1",
      "com.lihaoyi" %%% "scalatags" % "0.5.2"
    )
)

lazy val wbJS = wb.js
lazy val wbJVM = wb.jvm

