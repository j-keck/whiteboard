package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.MessageEvent
import wb.client.log.LogView
import wb.shared.Coordinate

import scala.scalajs.js

class WhiteBoard extends LogView {

  def draw(canvas: html.Canvas): Unit = {
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

    renderer.fillStyle = "#ededed"
    renderer.fillRect(0, 0, canvas.width, canvas.height)

    renderer.fillStyle = "black"
    var down = false
    canvas.onmousedown = (e: MouseEvent) => {
      debug("mouse-down")
      down = true
    }
    canvas.onmouseup = (_: MouseEvent) => {
      debug("mouse-up")
      down = false
    }


    class BoardSync extends WSSupport with LogView {
      val socket = run("/board")

      def send(coord: Coordinate): Unit = {
        socket.send(coord.toString)
      }
      def send(s: String) =  socket.send(s)


      socket.onmessage = (e: MessageEvent) => {
        val c = js.JSON.parse(e.data.toString)
        def d(dyn: Dynamic): Double = {
          val s = dyn.toString
          s.toDouble
        }
        renderer.fillRect(c.x.toString.toDouble, c.y.toString.toDouble, c.w.toString.toDouble, c.h.toString.toDouble)
      }
    }
    val boardSync = new BoardSync()


    canvas.onmousemove = (e: MouseEvent) => {
      val rect = canvas.getBoundingClientRect
      if (down) {
        val (x, y, w, h) =  (e.clientX - rect.left, e.clientY - rect.top, 5, 5)
        boardSync.send(s"""{"x":$x,"y":$y,"w":$w,"h":$h}""") // FIXME: compile 'argonaut' for scala.js
      }
    }
  }
}
