import sbt.dsl._

name := "whiteboard"

val http4sVersion = "0.8.1"


def WBPrj(name: String): Project = {
  Project(name, file(name)).
    settings(
      version := "0.0.1",
      scalaVersion := "2.11.7",
      resolvers += Resolver.bintrayRepo("j-keck", "maven"),
      resolvers += "Scalaz Bintray Repo" at "https://dl.bintray.com/scalaz/releases",
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      libraryDependencies ++= Seq(
        "io.circe" %% "circe-core" % "0.2.0-SNAPSHOT",
        "io.circe" %% "circe-generic" % "0.2.0-SNAPSHOT",
        "io.circe" %% "circe-jawn" % "0.2.0-SNAPSHOT"
      )
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
          "ch.qos.logback" % "logback-classic" % "1.1.3"
        )
    )
  )

lazy val client = (
  WBPrj("client").
    dependsOn(shared).
    enablePlugins(ScalaJSPlugin).
    settings(
      scalaJSStage in Global := FastOptStage,
      libraryDependencies ++=
        Seq(
          "org.scala-js" %%% "scalajs-dom" % "0.8.1",
          "com.lihaoyi" %%% "scalatags" % "0.5.2",
          "io.circe" %%% "circe-core" % "0.2.0-SNAPSHOT",
          "io.circe" %%% "circe-generic" % "0.2.0-SNAPSHOT",
          "io.circe" %%% "circe-parse" % "0.2.0-SNAPSHOT",
          "sodium" %%% "sodium" % "1.0")
    )
  )


