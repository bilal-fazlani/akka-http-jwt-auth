package tech.bilal.akka.http.oidc.client.models

import io.circe.Decoder
import io.circe.Codec.AsObject

case class Key(e: String, n: String, kty: String, kid: String) derives AsObject
