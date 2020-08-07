package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.model.HttpMethod
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.server.Directives.{
  authorize => akkaAuth,
  authorizeAsync => akkaAuthAsync,
  _
}
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.AuthenticationDirective
import tech.bilal.akka.http.auth.adapter.AuthorizationPolicy.PolicyExpression.{
  And,
  Or
}
import tech.bilal.akka.http.auth.adapter.AuthorizationPolicy.{
  CustomPolicy,
  CustomPolicyAsync,
  PolicyExpression
}

import scala.concurrent.ExecutionContext

class SecurityDirectives[T: TokenDecoder](
    authentication: SyncAuthenticatorFactory[T],
    realm: String
)(implicit
    ec: ExecutionContext
) {

  /**
    * Rejects all un-authorized requests
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def secure(
      authorizationPolicy: AuthorizationPolicy[T]
  ): Directive1[T] = {
    authenticateSync.flatMap(token =>
      authorize(authorizationPolicy, token) & provide(token)
    )
  }

  /**
    * Rejects all un-authorized and non-POST requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sPost(authorizationPolicy: AuthorizationPolicy[T]): Directive1[T] =
    sMethod(POST, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-GET requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sGet(authorizationPolicy: AuthorizationPolicy[T]): Directive1[T] =
    sMethod(GET, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-GET requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sPut(authorizationPolicy: AuthorizationPolicy[T]): Directive1[T] =
    sMethod(PUT, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-PUT requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sDelete(
      authorizationPolicy: AuthorizationPolicy[T]
  ): Directive1[T] = sMethod(DELETE, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-PATCH requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sPatch(
      authorizationPolicy: AuthorizationPolicy[T]
  ): Directive1[T] = sMethod(PATCH, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-HEAD requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sHead(authorizationPolicy: AuthorizationPolicy[T]): Directive1[T] =
    sMethod(HEAD, authorizationPolicy)

  /**
    * Rejects all un-authorized and non-CONNECT requests
    *
    * @param authorizationPolicy Authorization policy to use for filtering requests.
    *                            There are different types of authorization policies. See [[AuthorizationPolicy]]
    */
  def sConnect(
      authorizationPolicy: AuthorizationPolicy[T]
  ): Directive1[T] = sMethod(CONNECT, authorizationPolicy)

  def authenticateSync: AuthenticationDirective[T] =
    authenticateOAuth2(realm, authentication.make)

  def authorize(
      authorizationPolicy: AuthorizationPolicy[T],
      accessToken: T
  ): Directive0 =
    authorizationPolicy match {
      case CustomPolicy(predicate)      => akkaAuth(predicate(accessToken))
      case CustomPolicyAsync(predicate) => akkaAuthAsync(predicate(accessToken))
      case PolicyExpression(left, op, right) =>
        op match {
          case And =>
            authorize(left, accessToken) & authorize(right, accessToken)
          case Or =>
            authorize(left, accessToken) | authorize(right, accessToken)
        }
    }

  private def sMethod(
      httpMethod: HttpMethod,
      authorizationPolicy: AuthorizationPolicy[T]
  ): Directive1[T] =
    method(httpMethod) & secure(authorizationPolicy)
}
