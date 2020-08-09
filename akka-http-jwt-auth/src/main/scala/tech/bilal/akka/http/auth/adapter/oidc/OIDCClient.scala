package tech.bilal.akka.http.auth.adapter.oidc

import akka.actor.ClassicActorSystemProvider
import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class OIDCConfig(jwks_uri: String, issuer: String)
case class Key(e: String, n: String, kty: String, kid: String)
case class KeySet(keys: List[Key])
object KeySet {
  val empty: KeySet = KeySet(Nil)
}

class OIDCClient(wellKnownUrl: String, httpCMaybe: Option[HttpClient] = None)(
    implicit system: ClassicActorSystemProvider
) {
  private implicit val oidcConfigDec: Decoder[OIDCConfig] =
    MapBasedCodecs.deriveDecoder
  private implicit val keyDec: Decoder[Key] =
    MapBasedCodecs.deriveDecoder
  private implicit val keySetDec: Decoder[KeySet] =
    MapBasedCodecs.deriveDecoder

  private val httpC = httpCMaybe.getOrElse(new HttpClient)

  lazy val fetchOIDCConfig: Future[OIDCConfig] =
    httpC.get[OIDCConfig](wellKnownUrl)

  def fetchKeys: Future[KeySet] =
    for {
      config <- fetchOIDCConfig
      keys <- httpC.get[KeySet](config.jwks_uri)
    } yield keys
}
