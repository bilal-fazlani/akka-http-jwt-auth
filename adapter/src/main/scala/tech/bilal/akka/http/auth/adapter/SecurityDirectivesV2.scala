package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.{
  authorize => akkaAuth,
  authorizeAsync => akkaAuthAsync,
  _
}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.AuthenticationDirective

import scala.concurrent.{ExecutionContext, Future}

class SecurityDirectivesV2[T: TokenDecoder](
    authentication: SyncAuthenticatorFactory[T],
    realm: String
)(implicit
    ec: ExecutionContext
) {
  val auth = authentication.make

  def policy(policy: T => Boolean): Directive0 =
    authenticateSync.flatMap(t => akkaAuth(policy(t)))

  def policyAsync(policy: T => Future[Boolean]): Directive0 =
    authenticateSync.flatMap(t => akkaAuthAsync(policy(t)))

  def token: Directive1[T] = authenticateSync

  def authenticateSync: AuthenticationDirective[T] =
    authenticateOAuth2(realm, auth)
}
