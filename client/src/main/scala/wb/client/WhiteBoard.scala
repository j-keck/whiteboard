package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.MessageEvent
import sodium.{StreamSink, Stream, CellSink}
import wb.client.log.LogView
import wb.shared.Coordinate
import argonaut._, Argonaut._
import scalatags.JsDom.all._
import ReactiveElement._
import scalaz._, Scalaz._

import scala.scalajs.js

object WhiteBoard extends LogView {

  private val doc = dom.document

  sealed trait UpDown

  case object Up extends UpDown

  case object Down extends UpDown

  private val mouseState = new CellSink[UpDown](Up)

  type MousePos = (Double, Double)
  private val mousePos = new CellSink[MousePos](0, 0)


  def draw(): Unit = {
    // tool-bar
    doc.getElementById("toolBar").appendChild(toolBar)

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
      mousePos.send(e.clientX - rect.left, e.clientY - rect.top)
    }


    //
    // setup renderer
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // background
    renderer.fillStyle = "#ededed"
    renderer.fillRect(0, 0, canvas.width, canvas.height)

    // foreground
    renderer.fillStyle = "black"




    // board synchronization: client <-> server
    val boardSync = new WSSupport with LogView {
      val socket = run("/board")

      def send(coord: Coordinate): Unit = {
        socket.send(coord.asJson.nospaces)
      }

      socket.onmessage = (e: MessageEvent) => {
        val c = js.JSON.parse(e.data.toString)
        def d(dyn: Dynamic): Double = {
          val s = dyn.toString
          s.toDouble
        }
        renderer.fillRect(c.x.toString.toDouble, c.y.toString.toDouble, c.w.toString.toDouble, c.h.toString.toDouble)
      }
    }

    // mouse actions
    mousePos.value.gate(mouseState.map(_ == Down)).map { case (x, y) =>
      boardSync.send(Coordinate(x, y, 5, 5))
    }
  }

  def toolBar = {
    val colors = Seq("black", "red", "green", "yellow", "brown")
    val (btns, streams) = colors.map(c => mkButton(c, c)).unzip

    // FIXME
    val canvas = doc.getElementById("canvas").asInstanceOf[dom.html.Canvas]
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // change color
    streams.reduce(_ |+| _) map(renderer.fillStyle = _)

    div(btns).render
  }

  implicit def streamMonoid[A: Semigroup]: Monoid[Stream[A]] = new Monoid[Stream[A]] {
    def append(f1: Stream[A], f2: => Stream[A]): Stream[A] = f1.merge(f2)
    def zero: Stream[A] = new StreamSink[A]
  }
}
