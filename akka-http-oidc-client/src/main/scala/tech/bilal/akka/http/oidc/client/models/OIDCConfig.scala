package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder

case class OIDCConfig(jwks_uri: String, issuer: String)
object OIDCConfig {
    given Decoder[OIDCConfig] = Decoder.forProduct2("jwks_uri", "issuer")(OIDCConfig.apply)
}
