package wb

import argonaut.Argonaut._

package object shared {
  case class Pos(x: Double, y: Double)
  case class Line(start: Pos, end: Pos, color: String)

  implicit def PointCodecJson = casecodec2(Pos.apply, Pos.unapply)("x", "y")
  implicit def CoordinateCodecJson = casecodec3(Line.apply, Line.unapply)("start", "end", "color")
}
