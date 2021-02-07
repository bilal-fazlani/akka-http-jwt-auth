package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder

case class Key(e: String, n: String, kty: String, kid: String)
object Key {
  given Decoder[Key] = Decoder.forProduct4("e", "n", "kty", "kid")(Key.apply)
}
