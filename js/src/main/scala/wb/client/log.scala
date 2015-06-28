package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import scalatags.JsDom.all._

object log {

  private val doc = dom.document

  trait LogSupport {
    def debug(msg: String): Unit

    def info(msg: String): Unit

    def error(msg: String): Unit

    def exception(msg: String, e: Exception): Unit
  }

  object LogTable extends LogTable {
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

  trait LogTable extends LogSupport {
    override def debug(msg: String): Unit = append("DEBUG", msg)

    override def error(msg: String): Unit = append("ERROR", msg)

    override def info(msg: String): Unit = append("INFO", msg)

    override def exception(msg: String, e: Exception): Unit = append("EXCEPTION", msg)

    private def append(severity: String, msg: String): Unit = {
      val row = tr(
        td("<TS>"),
        td(severity),
        td(msg)
      )
      println("ROW:")
      println(row)
      doc.getElementById("log-table-body").appendChild(row.render)

    }
  }

}
