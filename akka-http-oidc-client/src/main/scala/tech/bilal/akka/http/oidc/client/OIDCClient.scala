package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.actor.typed.SpawnProtocol.Command
import akka.util.Timeout
import tech.bilal.akka.http.client.circe.HttpClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.ExecutionContext
import tech.bilal.akka.http.oidc.client.models._

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

class OIDCClient(wellKnownUrl: String, httpClient: HttpClient)(
    using actorSystem: ActorSystem[Command]
) {
  given Scheduler = actorSystem.scheduler
  val oidcConfig = LazySuccessCachedFuture[OIDCConfig](httpClient.get[OIDCConfig](wellKnownUrl))

  def fetchKeys(timeout: Timeout): Future[KeySet] = {
    for {
      config <- oidcConfig.future(timeout)
      keys <- httpClient.get[KeySet](config.jwks_uri)
    } yield keys
  }
}
