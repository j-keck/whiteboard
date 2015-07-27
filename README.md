# scala.js playground


__depends on scala.js-0.6.5-SNAPSHOT because argonaut use 
[StringBuilder.appendCodePoint(int)](https://github.com/scala-js/scala-js/pull/1792) which was missing in scala.js.__


super simple client / server whiteboard, to play around with 
  
  * scala.js
  * http4s
  * sbt multi project (js + jvm)


# dev

        > ~; client/fastOptJS; re-start
or       

        > ~; compile; re-start
        
only in the 'compile' and 'packageBin' task's the scala.js were generated FIXME: why? (hint: 'resourceGenerators')
