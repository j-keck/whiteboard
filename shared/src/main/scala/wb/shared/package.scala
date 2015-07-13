package wb

import argonaut.Argonaut._

package object shared {

  case class Pos(x: Double, y: Double)
  case class Style(color: String, width: Int)
  case class Line(start: Pos, end: Pos, style: Style)

  // json
  implicit def PointCodecJson = casecodec2(Pos.apply, Pos.unapply)("x", "y")
  implicit def StyleCodecJson = casecodec2(Style.apply, Style.unapply)("color", "width")
  implicit def CoordinateCodecJson = casecodec3(Line.apply, Line.unapply)("start", "end", "style")
}
