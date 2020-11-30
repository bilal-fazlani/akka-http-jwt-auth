package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import io.bullet.borer.Decoder
import tech.bilal.akka.http.oidc.client.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OIDCClient(wellKnownUrl: String, httpCMaybe: Option[HttpClient] = None)(
    using system: ClassicActorSystemProvider
) {
  private lazy val httpC = httpCMaybe.getOrElse(HttpClient())

  lazy val fetchOIDCConfig: Future[OIDCConfig] =
    httpC.get[OIDCConfig](wellKnownUrl)

  def fetchKeys: Future[KeySet] =
    for {
      config <- fetchOIDCConfig
      keys <- httpC.get[KeySet](config.jwks_uri)
    } yield keys
}
