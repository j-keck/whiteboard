package wb.client

import org.scalajs.dom
import wb.client.log.LogView

import scala.scalajs.js.JSApp


object Main extends JSApp with LogView {


  val doc = dom.document

  def main(): Unit = {
    // init log-table
    LogView.appendLogTableTo(doc.getElementById("log"))

    // whiteboard
    WhiteBoard.draw()
  }
}
