package wb.client


import org.scalajs.dom
import org.scalajs.dom._

trait WSSupport {
  self: log.LogSupport =>

  val doc = dom.document

  def run(endpoint: String): WebSocket = {
    val socket = webSocket(endpoint)
    socket.onopen = (e: Event) => debug(s"ws connected (endpoint: ${endpoint})")
    socket.onerror = (e: ErrorEvent) => error(s"ws error (endpoint: ${endpoint}) error: ${e.message}")
    socket.onclose = (e: Event) => debug(s"ws closed (endpoint: ${endpoint})")
    socket
  }


  private def webSocket(endpoint: String): WebSocket = {
    val proto = if(doc.location.protocol == "https:") "wss" else "ws"
    val url = s"${proto}://${doc.location.host}/${endpoint}"
    new WebSocket(url)
  }
}
