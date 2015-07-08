package wb.client

import org.scalajs.dom
import sodium.{CellSink, StreamSink, Cell, Stream}

import scala.concurrent.duration.Duration

object sodiumExtensions {

  implicit class CellOps[A](ca: Cell[A]){
    def zip[B](cb: Cell[B]): Cell[(A, B)] =
      ca.lift((a, b: B) => (a, b), cb)
  }

  implicit class StreamOps[A](sa: Stream[A]){
    def accum[S](s: S)(f: (A, S) => S): Cell[S] = sa.accum(s, f)
  }


  private def every(duration: Duration): Cell[Long] = {
    val ticker = new CellSink[Long](System.currentTimeMillis())
    dom.setTimeout(() => ticker.send(System.currentTimeMillis()), duration.toMillis)
    ticker
  }
}
