package tech.bilal.akka.http.auth.adapter.models

import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs

case class KeySet(keys: List[Key])
object KeySet {
  val empty: KeySet = KeySet(Nil)
  implicit val keySetDec: Decoder[KeySet] =
    MapBasedCodecs.deriveDecoder
}
