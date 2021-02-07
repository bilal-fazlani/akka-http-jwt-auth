package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder
import io.circe.Codec.AsObject

case class OIDCConfig(jwks_uri: String, issuer: String) derives AsObject