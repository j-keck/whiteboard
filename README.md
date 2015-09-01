# scala.js playground

super simple multiuser client / server whiteboard, to play around with
 
  * [scala.js](https://github.com/scala-js/scala-js)
  * [http4s](https://github.com/http4s/http4s)
  * [circe](https://github.com/travisbrown/circe)
  * sbt multi project (js + jvm)


# dev

        > ~; client/fastOptJS; re-start
or       

        > ~; compile; re-start
        
only in the 'compile' and 'packageBin' task's the scala.js were generated FIXME: why? (hint: 'resourceGenerators')
