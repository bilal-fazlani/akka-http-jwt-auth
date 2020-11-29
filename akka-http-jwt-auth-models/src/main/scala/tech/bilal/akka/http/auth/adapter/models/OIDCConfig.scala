package tech.bilal.akka.http.auth.adapter.models

import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs

case class OIDCConfig(jwks_uri: String, issuer: String)
object OIDCConfig {
  implicit val oidcConfigDec: Decoder[OIDCConfig] =
    MapBasedCodecs.deriveDecoder
}
