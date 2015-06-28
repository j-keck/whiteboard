import sbt.dsl._

name := "whiteboard"

val http4sVersion = "0.8.1"

def WBPrj(name: String): Project = {
  Project(name, file(name)).
    settings(
      version := "0.0.1",
      scalaVersion := "2.11.6"
    )
}

lazy val shared = (
  WBPrj("shared").
  enablePlugins(ScalaJSPlugin)
  )

lazy val server = (
  WBPrj("server").
    dependsOn(shared).
    settings(
      /*
        resources in Compile += (fastOptJS in Compile in client).value.data,
        resourceGenerators in Compile += Def.task {
          val trg = (resourceManaged in Compile).value / "jsStuff.js"
          IO.copyFile((fastOptJS in Compile in client).value.data, trg)
          Seq(trg)
        }.taskValue,
        */
      compile <<= (compile in Compile) dependsOn (fastOptJS in Compile in client),
     crossTarget in (fastOptJS in Compile in client) :=
        ((classDirectory in Compile in shared).value / "public" / "js"),
      libraryDependencies ++=
        Seq(
          "org.http4s" %% "http4s-dsl" % http4sVersion,
          "org.http4s" %% "http4s-blazeserver" % http4sVersion,
          "ch.qos.logback" % "logback-classic" % "1.1.3",
          "io.argonaut" %% "argonaut" % "6.0.4"
        )
    )
  )

lazy val client = (
  WBPrj("client").
    dependsOn(shared).
    enablePlugins(ScalaJSPlugin).
    settings(
    // crossTarget in (fastOptJS in Compile in server) :=
     //   ((classDirectory in Compile in server).value / "public" / "js"),
      libraryDependencies ++=
        Seq(
          "org.scala-js" %%% "scalajs-dom" % "0.8.1",
          "com.lihaoyi" %%% "scalatags" % "0.5.2"
        )
    )
  )

