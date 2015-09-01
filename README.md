# scala.js playground

super simple multiuser client / server whiteboard, to play around with
 
  * [scala.js](https://github.com/scala-js/scala-js)
  * [http4s](https://github.com/http4s/http4s)
  * [argonaut](https://github.com/argonaut-io/argonaut)
  * sbt multi project (js + jvm)

# branches

## master
   * json parsing per argonaut
    
## circe-based
   * same as master
   * use circe for json handling

## sbt-scalajs-crossproject
   * cross project per 'crossProject ...'
   * works under sbt, but not under idea

# dev

        > ~; client/fastOptJS; re-start
or       

        > ~; compile; re-start
        
only in the 'compile' and 'packageBin' task's the scala.js were generated FIXME: why? (hint: 'resourceGenerators')
