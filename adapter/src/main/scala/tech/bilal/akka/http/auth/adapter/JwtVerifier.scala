package tech.bilal.akka.http.auth.adapter

import java.security.PublicKey

import io.bullet.borer.derivation.MapBasedCodecs
import io.bullet.borer.{Decoder, Json}
import pdi.jwt.{Jwt, JwtOptions}
import tech.bilal.akka.http.auth.adapter.oidc.{OIDCClient, PublicKeyManager}

import scala.Option
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

//todo:
// create public key
// validate token using public key
// parse token into Option[T]

class JwtVerifier(oidcClient: OIDCClient, publicKeyManager: PublicKeyManager) {
  def verifyAndDecode[T: Decoder](tokenString: String): Future[Option[T]] =
    for {
      (kid, contents) <- fromOption(getKIDAndContents(tokenString))
      key <- publicKeyManager.getKey(kid)
      issuer <- oidcClient.fetchOIDCConfig.map(_.issuer)
      _ = Jwt.validate(tokenString, JwtOptions(true, true, true),)
    } yield ???

  private def fromOption[T](option: Option[T]): Future[T] =
    option match {
      case Some(value) => Future.successful(value)
      case None        => Future.failed(new RuntimeException("no value found"))
    }

  private def flip[T](option: Option[Future[T]]): Future[Option[T]] =
    option match {
      case Some(f) => f.map(x => Some(x))
      case None    => Future.successful(None)
    }

//  private def flip[T](future: Future[Option[T]]): Option[Future[T]] =

  private case class HeaderWithKid(kid: Option[String])
  private implicit val dec: Decoder[HeaderWithKid] =
    MapBasedCodecs.deriveDecoder[HeaderWithKid]

  private def getKIDAndContents(header: String): Option[(String, String)] =
    Jwt.decodeRawAll(
      header,
      JwtOptions(signature = false, expiration = false, notBefore = false)
    ) match {
      case Success((header, contents, _)) =>
        Json
          .decode(header.getBytes)
          .to[HeaderWithKid]
          .value
          .kid
          .map((_, contents))
      case Failure(_) => None
    }
}
