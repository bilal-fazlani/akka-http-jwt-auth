package tech.bilal.akka.http.oidc.client.models

import io.bullet.borer.Decoder

case class OIDCConfig(jwks_uri: String, issuer: String)
object OIDCConfig {
  given oidcConfigDec as Decoder[OIDCConfig] =
    Decoder.from(OIDCConfig.apply _)
}
