package tech.bilal.akka.http.auth.adapter

import akka.http.scaladsl.server.Directives.AsyncAuthenticator
import akka.http.scaladsl.server.directives.Credentials.Provided

import scala.concurrent.Future
import scala.reflect.ClassTag

class AsyncAuthenticatorFactory[T: ClassTag](jwtVerifier: JwtVerifier) {
  def make: AsyncAuthenticator[T] = {
    case Provided(identifier) =>
      jwtVerifier.verifyAndDecode[T](identifier)
    case _ =>
      Future.successful(None)
  }
}
