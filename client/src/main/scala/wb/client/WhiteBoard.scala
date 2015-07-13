package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.MessageEvent
import sodium.{Cell, StreamSink, Stream, CellSink}
import wb.client.log.LogView
import wb.shared.{Style, Pos, Line}
import argonaut._, Argonaut._
import scala.concurrent.duration._
import scalatags.JsDom.all._
import ReactiveElement._


import scala.scalajs.js

object WhiteBoard extends LogView {

  private val doc = dom.document

  sealed trait UpOrDown {
    def isUp = this == Up

    def isDown = this == Down
  }

  case object Up extends UpOrDown

  case object Down extends UpOrDown

  private val mouseState = new CellSink[UpOrDown](Up)

  private val mousePos = new CellSink[Pos](Pos(0, 0))



  def draw(): Unit = {
    // the line style (color / width)
    val style: Cell[Style] = {

      val color = {
        val (div, color) = colorToolBar
        doc.getElementById("toolBar").appendChild(div)
        color
      }

      val lineWidth = {
        val (div, lineWidth) = lineWidthToolBar
        doc.getElementById("toolBar").appendChild(div)
        lineWidth
      }

      color lift(Style(_, _: Int), lineWidth)
    }


    //
    // register mouse listeners
    doc.onmousedown = (e: MouseEvent) => {
      // react only on the left mouse button
      if (e.button == 0)
        mouseState.send(Down)
    }

    doc.onmouseup = (_: MouseEvent) => {
      mouseState.send(Up)
    }


    //
    // setup canvas
    val canvas = doc.getElementById("canvas").asInstanceOf[dom.html.Canvas]
    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

    canvas.onmousemove = (e: MouseEvent) => {
      val rect = canvas.getBoundingClientRect
      mousePos.send(Pos(e.clientX - rect.left, e.clientY - rect.top))
    }


    //
    // setup renderer
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // background
    renderer.fillStyle = "#ededed"
    renderer.fillRect(0, 0, canvas.width, canvas.height)


    // board synchronization: client <-> server
    val boardSync = new WSSupport with LogView {
      val socket = run("/board")

      def send(line: Line): Unit = {
        socket.send(line.asJson.nospaces)
      }

      socket.onmessage = (e: MessageEvent) => {
        e.data.toString.decodeEither[Line].fold(error,  line => {
          renderer.beginPath()
          renderer.strokeStyle = line.style.color
          renderer.lineWidth = line.style.width
          renderer.moveTo(line.start.x, line.start.y)
          renderer.lineTo(line.end.x, line.end.y)
          renderer.stroke()
        })
      }
    }



    mousePos.value.snapshot(mouseState, (p, s: UpOrDown) => (p, s))

    import sodiumExtensions._

    StreamOps(mousePos.zip(mouseState).value).accum(Vector.empty[Pos])({ case ((p, s), v) => (v, s) match {
      case (Vector(start, end), Down) =>
        boardSync.send(Line(start, end, style.sample))
        Vector(end)
      case (_, Down) => v :+ p
      case (_, Up) => Vector.empty
    }})

  }

  def colorToolBar: (Node, Cell[String]) = {
    val colors = Seq("black", "red", "green", "yellow", "brown")
    val (btns, streams) = colors.map(c => mkButton(c, c)).unzip

    (div(btns).render, streams.reduce(_ merge _).hold("black"))
  }

  def lineWidthToolBar: (Node, Cell[Int]) = {
    val sizes = 1 to 5
    val (btns, streams) = sizes.map(s => mkButton(s"$s", s)).unzip

    (div(btns).render, streams.reduce(_ merge _).hold(1))
  }

}
