package wb

package object shared {

  case class Pos(x: Double, y: Double)
  case class Style(color: String, width: Int)
  case class Line(start: Pos, end: Pos, style: Style)

}
