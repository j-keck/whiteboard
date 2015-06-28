package wb.server

import java.net.InetSocketAddress
import java.nio.file.Paths
import java.util.concurrent.Executors

import org.http4s.StaticFile
import org.http4s.blaze.channel.SocketConnection
import org.http4s.blaze.channel.nio1.NIO1SocketServerGroup
import org.http4s.blaze.pipeline.LeafBuilder
import org.http4s.server.blaze.{WebSocketSupport, Http1ServerStage}
import org.http4s.server.websocket.WS
import wb.shared.Coordinate

import scala.concurrent.duration._

import org.http4s.dsl.{->, /, Root, _}
import org.http4s.server.HttpService
import org.http4s.websocket.WebsocketBits.{WebSocketFrame, Text}

import _root_.scalaz.stream.{Exchange, Process, time}
import scalaz.concurrent.Task
import scalaz.stream.async.topic
import argonaut._, Argonaut._

object Server extends App {
  implicit val scheduledEC = Executors.newScheduledThreadPool(1)

  private val coordinates = topic[Coordinate]()
  coordinates.subscribe.map(println).run.runAsync(_ => ())

  implicit def CoordinateCodecJson = casecodec4(Coordinate.apply, Coordinate.unapply)("x", "y", "w", "h")

  val service = HttpService {
    case r@GET -> Root / "chat" =>
      val src = time.awakeEvery(10.seconds).map(d => Text(s"dummy msg: ${d.toSeconds}"))
      WS(Exchange(src, Process.halt))

    case r @ GET -> Root / "board" =>
      def txtFrame2Coordinate(frame: WebSocketFrame) = frame match {
        case Text(txt, _) => txt.decode[Coordinate].getOrElse(Coordinate(0, 0, 0, 0)) //FIXME
      }

      val src = coordinates.subscribe.map(c => Text(c.asJson.spaces2))
      val snk = coordinates.publish.map(_ compose txtFrame2Coordinate)//(_ => Process.halt)
      WS(Exchange(src, snk))

    case req@GET -> Root / "app.js" =>
      val path = Paths.get(getClass.getResource("/").toURI).getParent.resolve("whiteboard-fastopt.js")
      StaticFile.fromFile(path.toFile, Some(req)).map(Task.now).get // FIXME

    case req@GET -> Root =>
      StaticFile.fromResource("/index.html").map(Task.now).get // FIXME
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
