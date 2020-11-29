package tech.bilal.akka.http.oidc.client.models

import io.bullet.borer.Decoder

case class Key(e: String, n: String, kty: String, kid: String)
object Key {
  given keyDec as Decoder[Key] =
    Decoder.from(Key.apply _)
}
