package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder

case class KeySet(keys: List[Key])

object KeySet {
  val empty: KeySet = KeySet(Nil)
  given Decoder[KeySet] = Decoder.forProduct1("keys")(KeySet.apply)
}
