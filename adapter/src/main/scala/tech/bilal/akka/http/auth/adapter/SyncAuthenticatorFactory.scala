package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.{AsyncAuthenticator, Authenticator}
import akka.http.scaladsl.server.directives.Credentials.Provided
import io.bullet.borer.Decoder

class SyncAuthenticatorFactory[T: Decoder](jwtVerifier: JwtVerifier) {
  def make: Authenticator[T] = {
    case Provided(identifier) =>
      jwtVerifier.verifyAndDecode[T](identifier)
    case _ =>
      None
  }
}
