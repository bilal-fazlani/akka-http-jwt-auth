package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.{authorize => akkaAuth, authorizeAsync => akkaAuthAsync, _}
import akka.http.scaladsl.server._
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class AuthDirectives[T: ClassTag](
    authentication: AsyncAuthenticatorFactory[T],
    realm: String
)(using
    ec: ExecutionContext
) {
  private val auth = authentication.make

  def policy(policy: T => Boolean): Directive0 =
    token.flatMap(t => akkaAuth(policy(t)))

  def asyncPolicy(policy: T => Future[Boolean]): Directive0 =
    token.flatMap(t => akkaAuthAsync(policy(t)))

  def token: Directive1[T] = authenticateOAuth2Async(realm, auth)
}
