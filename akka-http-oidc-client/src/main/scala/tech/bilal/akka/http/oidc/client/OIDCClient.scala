package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import io.bullet.borer.Decoder
import tech.bilal.akka.http.oidc.client.models._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OIDCClient(wellKnownUrl: String, httpCMaybe: Option[HttpClient] = None)(
    implicit system: ClassicActorSystemProvider
) {
  private val httpC = httpCMaybe.getOrElse(new HttpClient)

  lazy val fetchOIDCConfig: Future[OIDCConfig] =
    httpC.get[OIDCConfig](wellKnownUrl)

  def fetchKeys: Future[KeySet] =
    for {
      config <- fetchOIDCConfig
      keys <- httpC.get[KeySet](config.jwks_uri)
    } yield keys
}
