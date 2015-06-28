import sbt.dsl._

name := "whiteboard"

val http4sVersion = "0.8.1"

scalaVersion := "2.11.6"

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
      Revolver.settings,
      resourceGenerators in Compile <+= Def.task {
        val files = ((crossTarget in(client, Compile)).value ** ("*.js" || "*.map")).get
        val mappings: Seq[(File, String)] = files pair rebase((crossTarget in(client, Compile)).value, ((resourceManaged in Compile).value).getAbsolutePath)
        val map: Seq[(File, File)] = mappings.map { case (s, t) => (s, file(t)) }
        IO.copy(map).toSeq
      },

      compile <<= (compile in Compile) dependsOn (fastOptJS in Compile in client),

      libraryDependencies ++=
        Seq(
          "org.http4s" %% "http4s-dsl" % http4sVersion,
          "org.http4s" %% "http4s-blazeserver" % http4sVersion,
          "ch.qos.logback" % "logback-classic" % "1.1.3",
          "io.argonaut" %% "argonaut" % "6.1"
        )
    )
  )

lazy val client = (
  WBPrj("client").
    dependsOn(shared).
    enablePlugins(ScalaJSPlugin).
    settings(
      libraryDependencies ++=
        Seq(
          "org.scala-js" %%% "scalajs-dom" % "0.8.1",
          "com.lihaoyi" %%% "scalatags" % "0.5.2"
        )
    )
  )

