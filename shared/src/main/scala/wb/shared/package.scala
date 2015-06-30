package wb

import argonaut.Argonaut._

package object shared {
  case class Coordinate(x: Double, y: Double, w: Double, h: Double)
  implicit def CoordinateCodecJson = casecodec4(Coordinate.apply, Coordinate.unapply)("x", "y", "w", "h")
}
