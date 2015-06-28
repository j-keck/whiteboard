package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.MessageEvent
import sodium.CellSink
import wb.client.log.LogView
import wb.shared.Coordinate

import scala.scalajs.js

class WhiteBoard extends LogView {

  sealed trait UpDown

  case object Up extends UpDown

  case object Down extends UpDown

  private val mouseState = new CellSink[UpDown](Up)

  type MousePos = (Double, Double)
  private val mousePos = new CellSink[MousePos](0, 0)


  def draw(canvas: html.Canvas): Unit = {
    //
    // configure canvas
    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

    //
    // setup renderer
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    // background
    renderer.fillStyle = "#ededed"
    renderer.fillRect(0, 0, canvas.width, canvas.height)

    // foreground
    renderer.fillStyle = "black"


    //
    // register mouse listeners
    canvas.onmousedown = (e: MouseEvent) => {
      debug("mouse-down")
      mouseState.send(Down)
    }

    canvas.onmouseup = (_: MouseEvent) => {
      debug("mouse-up")
      mouseState.send(Up)
    }

    canvas.onmousemove = (e: MouseEvent) => {
      val rect = canvas.getBoundingClientRect
      mousePos.send(e.clientX - rect.left, e.clientY - rect.top)
    }



    val boardSync = new WSSupport with LogView {
      val socket = run("/board")

      def send(coord: Coordinate): Unit = {
        socket.send(coord.toString)
      }

      def send(s: String) = socket.send(s)


      socket.onmessage = (e: MessageEvent) => {
        val c = js.JSON.parse(e.data.toString)
        def d(dyn: Dynamic): Double = {
          val s = dyn.toString
          s.toDouble
        }
        renderer.fillRect(c.x.toString.toDouble, c.y.toString.toDouble, c.w.toString.toDouble, c.h.toString.toDouble)
      }
    }

    mousePos.value.gate(mouseState.map(_ == Down)).map { case (x, y) =>
      boardSync.send( s"""{"x":$x,"y":$y,"w":5,"h":5}""") // FIXME: compile 'argonaut' for scala.js
    }
  }

}
