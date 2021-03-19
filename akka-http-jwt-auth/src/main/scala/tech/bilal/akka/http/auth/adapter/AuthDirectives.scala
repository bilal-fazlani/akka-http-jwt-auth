package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.{authorize as akkaAuth, authorizeAsync as akkaAuthAsync, *}
import akka.http.scaladsl.server.*
import io.circe.Decoder
import akka.actor.typed.ActorSystem
import akka.actor.typed.SpawnProtocol.Command
import scala.concurrent.{ExecutionContext, Future}
import tech.bilal.akka.http.oidc.client.OIDCClient

class AuthDirectives[T: Decoder](
    authentication: AsyncAuthenticatorFactory[T],
    authConfig: AuthConfig
)(using
    ec: ExecutionContext
) {
  private val auth = authentication.make

  def policy(policy: T => Boolean): Directive0 =
    token.flatMap(t => akkaAuth(policy(t)))

  def asyncPolicy(policy: T => Future[Boolean]): Directive0 =
    token.flatMap(t => akkaAuthAsync(policy(t)))

  def token: Directive1[T] = authenticateOAuth2Async(authConfig.realm, auth)
}
object AuthDirectives {
  def apply[T:Decoder](authConfig:AuthConfig)
    (using actorSystem: ActorSystem[Command])
    :AuthDirectives[T] = {
      given ExecutionContext = actorSystem.executionContext
      val oIDCClient:OIDCClient = OIDCClient(authConfig.openIdConfigUrl)
      val pkm = PublicKeyManager(oIDCClient, authConfig)
      val verifier = JwtVerifier(oIDCClient.oidcConfig, pkm, authConfig)
      val authFactory = AsyncAuthenticatorFactory[T](verifier)
      new AuthDirectives(authFactory, authConfig)
    }
}