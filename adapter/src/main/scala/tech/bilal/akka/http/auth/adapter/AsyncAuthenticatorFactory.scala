package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.AsyncAuthenticator
import akka.http.scaladsl.server.directives.Credentials.Provided
import io.bullet.borer.Decoder

class AsyncAuthenticatorFactory[T: Decoder](jwtVerifier: JwtVerifier) {
  def make: AsyncAuthenticator[T] = {
    case Provided(identifier) =>
      jwtVerifier.verifyAndDecode[T](identifier)
    case _ =>
      None
  }
}
