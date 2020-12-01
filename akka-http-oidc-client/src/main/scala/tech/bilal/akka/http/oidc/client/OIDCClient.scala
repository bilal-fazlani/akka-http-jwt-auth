package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider

import scala.concurrent.ExecutionContext
//import io.bullet.borer.Decoder
import scala.reflect.runtime.universe._
import tech.bilal.akka.http.oidc.client.models._

//import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OIDCClient(wellKnownUrl: String, httpCMaybe: Option[HttpClient] = None)(
    using system: ClassicActorSystemProvider
) {
  private lazy val httpC = httpCMaybe.getOrElse(HttpClient())

  given ExecutionContext = scala.concurrent.ExecutionContext.global
  given TypeTag[OIDCConfig] = typeTag
  given TypeTag[KeySet] = typeTag
  
  lazy val fetchOIDCConfig: Future[OIDCConfig] =
    httpC.get[OIDCConfig](wellKnownUrl)

  def fetchKeys: Future[KeySet] =
    for {
      config <- fetchOIDCConfig
      keys <- httpC.get[KeySet](config.jwks_uri)
    } yield keys
}
