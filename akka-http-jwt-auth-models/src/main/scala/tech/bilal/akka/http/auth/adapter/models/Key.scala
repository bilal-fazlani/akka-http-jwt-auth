package tech.bilal.akka.http.auth.adapter.models

import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs

case class Key(e: String, n: String, kty: String, kid: String)
object Key {
  implicit val keyDec: Decoder[Key] =
    MapBasedCodecs.deriveDecoder
}
