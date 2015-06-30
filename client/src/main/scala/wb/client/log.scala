package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import scala.scalajs.js
import scalatags.JsDom.all._

object log {

  private val doc = dom.document

  trait LogSupport {

    def debug(msg: String) = log(now, "DEBUG", msg)

    def info(msg: String) = log(now, "INFO", msg)

    def error(msg: String) = log(now, "ERROR", msg)

    def exception(msg: String, e: Exception) = log(now, "EXCEPTION", s"${msg}: ${e.getMessage}")

    def log(ts: String, severity: String, msg: String): Unit

    private def now: String = new js.Date(js.Date.now()).toLocaleTimeString
  }

  
  object LogView extends LogView {

    def appendLogTableTo(e: Element): Unit = {
      val headers = Seq("Timestamp", "Severity", "Message")
      val logTable = table(
        thead(
          tr(for (h <- headers) yield td(h))
        ),
        tbody(id := "log-table-body")()
      )
      e.appendChild(logTable.render)
    }
  }

  trait LogView extends LogSupport {
    def log(ts: String, severity: String, msg: String): Unit = {
      val row = tr(
        td(ts),
        td(severity),
        td(msg)
      )
      doc.getElementById("log-table-body").appendChild(row.render)
    }
  }

}
