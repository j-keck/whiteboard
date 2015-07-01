package wb.client

import org.scalajs.dom
import org.scalajs.dom._
import org.scalajs.dom.raw.{HTMLTextAreaElement, HTMLButtonElement}
import sodium.{CellSink, Cell, StreamSink, Stream}

object ReactiveElement {

  private val doc = dom.document

  def mkButton(label: String): (Element, Stream[Unit]) = {
    val b = doc.createElement("button").asInstanceOf[HTMLButtonElement]
    b.textContent = label

    val out = new StreamSink[Unit]
    b.onclick = (_: Event) => {
      out.send(())
    }
    (b, out)
  }

  def mkButton[T](label: String, f: => T): (Element, Stream[T]) = {
    val (b, s) = mkButton(label)
    (b, s.map(_ => f))
  }

  def mkTextArea(in: Cell[String] = new Cell("")): (Element, Cell[String]) = {
    val t = doc.createElement("textarea").asInstanceOf[HTMLTextAreaElement]
    val out = new CellSink(t.value)

    t.textContent = in.sample()
    in.map{ v =>
      t.value = v
      out.send(v)
    }

    t.oninput = (e: Event) => {
      out.send(t.value)
    }

    (t, out)
  }

  def mkTextSpan(in: Cell[String]): Element = {
    val t = doc.createElement("span")
    t.innerHTML = in.sample()
    in.map(t.innerHTML = _)
    t
  }
}
