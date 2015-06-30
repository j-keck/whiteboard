package wb.server

import java.net.InetSocketAddress
import java.util.concurrent.Executors


import argonaut._, Argonaut._
import org.http4s.StaticFile
import org.http4s.blaze.channel.SocketConnection
import org.http4s.blaze.channel.nio1.NIO1SocketServerGroup
import org.http4s.blaze.pipeline.LeafBuilder
import org.http4s.dsl.{->, /, Root, _}
import org.http4s.server.HttpService
import org.http4s.server.blaze.{Http1ServerStage, WebSocketSupport}
import org.http4s.server.websocket.WS
import org.http4s.websocket.WebsocketBits.{Text, WebSocketFrame}
import wb.shared.Coordinate

import _root_.scalaz.stream.{Exchange, Process}
import scalaz.concurrent.Task
import scalaz.stream.async.topic

object Server extends App {
  implicit val scheduledEC = Executors.newScheduledThreadPool(1)

  private val coordinates = topic[Coordinate]()


  val service = HttpService {
    case r@GET -> Root / "board" =>
      def txtFrame2Coordinate(frame: WebSocketFrame) = frame match {
        case Text(txt, _) => txt.decodeEither[Coordinate].fold({ l: String =>
          println(l)
          Coordinate(0, 0, 0, 0) // FIXME
        }, identity)
      }

      val src: Process[Task, Text] = coordinates.subscribe.map(c => Text(c.asJson.spaces2))
      val snk = coordinates.publish.contramap(txtFrame2Coordinate) //(_ => Process.halt)
      WS(Exchange(src, snk))

    case req @ GET -> Root / name =>
      StaticFile.fromResource(s"/${name}", Some(req)).fold(NotFound(s"'${name}' not found"))(Task.now)

    case GET -> Root => PermanentRedirect(uri("/index.html"))
  }


  private val pool = Executors.newCachedThreadPool()

  def pipelineBuilder(conn: SocketConnection) = {
    val s = new Http1ServerStage(service, Some(conn), pool) with WebSocketSupport
    LeafBuilder(s)
  }

  val addr = new InetSocketAddress("0.0.0.0", 8888)

  NIO1SocketServerGroup.fixedGroup(4, 16*1024)
    .bind(addr, pipelineBuilder)
    .get    // yolo!
    .join()

}
