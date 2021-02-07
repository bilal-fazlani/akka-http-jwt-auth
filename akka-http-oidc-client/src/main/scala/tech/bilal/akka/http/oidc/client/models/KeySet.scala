package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder
import io.circe.Codec.AsObject

case class KeySet(keys: List[Key]) derives AsObject

object KeySet {
  val empty: KeySet = KeySet(Nil)
}
