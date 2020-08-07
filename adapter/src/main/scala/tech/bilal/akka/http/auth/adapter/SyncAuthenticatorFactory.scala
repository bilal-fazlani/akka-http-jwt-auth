package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.Authenticator
import akka.http.scaladsl.server.directives.Credentials.Provided

class SyncAuthenticatorFactory[T: TokenDecoder]() {
  def make: Authenticator[T] = {
    case Provided(identifier) =>
      val token = implicitly[TokenDecoder[T]].decode(identifier)
      token
    case _ =>
      None
  }
}
