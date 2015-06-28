package wb.client

import org.scalajs.dom
import org.scalajs.dom.raw.MessageEvent
import org.scalajs.dom.{html, MouseEvent}
import wb.client.log.LogView
import wb.shared.Coordinate

import scala.scalajs.js
import scala.scalajs.js.JSApp
import scala.scalajs.js.annotation.JSExport


object Main extends JSApp with LogView {


  val doc = dom.document

  def main(): Unit = {
    // init log-table
    LogView.appendLogTableTo(doc.getElementById("log"))

    // whiteboard
    val canvas = dom.document.getElementById("canvas").asInstanceOf[html.Canvas]
    val whiteBoard = new WhiteBoard()
    whiteBoard.draw(canvas)
  }
}
