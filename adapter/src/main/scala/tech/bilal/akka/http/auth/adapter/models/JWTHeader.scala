package tech.bilal.akka.http.auth.adapter.models

import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs

case class JWTHeader(
    typ: Option[String] = None,
    kid: String,
    alg: String
)

object JWTHeader {
  import io.bullet.borer.NullOptions._
  implicit val dec: Decoder[JWTHeader] = MapBasedCodecs.deriveDecoder
}
