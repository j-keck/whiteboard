package wb.client

import org.scalajs.dom
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{html, MouseEvent}
import wb.client.log.LogTable
import wb.shared.Coordinate

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport


object Main extends JSApp with LogTable {


  val doc = dom.document

  def main(): Unit = {
    println("in main")

    // init log-table
    LogTable.appendLogTableTo(doc.getElementById("log"))

    // dummy logs
    val x = new WSSupport with LogTable
    x.run("/chat").onmessage = (e: MessageEvent) => debug(e.data.toString)


    // "grayboard"
    val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
    val renderer = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    canvas.width = canvas.parentElement.clientWidth
    canvas.height = canvas.parentElement.clientHeight

    renderer.fillStyle = "gray"
    renderer.fillRect(0, 0, canvas.width, canvas.height)

    renderer.fillStyle = "black"
    var down = false
    canvas.onmousedown = (e: MouseEvent) => down = true
    canvas.onmouseup = (_: MouseEvent) => down = false


    class BoardSync extends WSSupport with LogTable {
      val socket = run("/board")

      def send(coord: Coordinate): Unit = {
        socket.send(coord.toString)
      }


      socket.onmessage = (e: MessageEvent) => {
        val c = js.JSON.parse(e.data.toString)
        def d(d: Dynamic): Double = d.toString.toDouble
        renderer.fillRect(d(c.x), d(c.y), d(c.w), d(c.h))
      }
    }
    val boardSync = new BoardSync()


    canvas.onmousemove = (e: MouseEvent) => {
      val rect = canvas.getBoundingClientRect
      if (down) {
        val (x, y, w, h) =  (e.clientX - rect.left, e.clientY - rect.top, 10, 10)
        boardSync.send(Coordinate(x, y, w, h))
        renderer.fillRect(x, y, w, h)
      }
    }
  }
}
