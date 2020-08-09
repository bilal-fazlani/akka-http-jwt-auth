package tech.bilal.akka.http.auth.adapter

import io.bullet.borer.{Decoder, Json}
import pdi.jwt.{Jwt, JwtOptions}
import tech.bilal.akka.http.auth.adapter.crypto.Algorithm
import tech.bilal.akka.http.auth.adapter.models.JWTHeader
import tech.bilal.akka.http.auth.adapter.oidc.{OIDCClient, PublicKeyManager}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class JwtVerifier(oidcClient: OIDCClient, publicKeyManager: PublicKeyManager) {
  def verifyAndDecode[T: Decoder](tokenString: String): Future[Option[T]] = {
    val jwtOptions =
      JwtOptions(signature = true, expiration = true, notBefore = true)
    for {
      (header, jsonPayloadContents) <-
        Future.fromTry(getKIDAndContents(tokenString))
      keyMayBe <- publicKeyManager.getKey(header.kid)
      key = keyMayBe.getOrElse(
        throw new RuntimeException(
          s"could not find key with kid: ${header.kid}"
        )
      )
      issuer <- oidcClient.fetchOIDCConfig.map(_.issuer)
      publicKey <- Future.fromTry(
        Algorithm("RSA").flatMap(_.publicKey(key, header))
        //todo: should we default to RSA?
      )
      _ = Jwt.validate(tokenString, publicKey, jwtOptions)
      token <-
        Future.fromTry(Json.decode(jsonPayloadContents.getBytes).to[T].valueTry)
    } yield Some(token)
  }

  private def getKIDAndContents(header: String): Try[(JWTHeader, String)] =
    Jwt
      .decodeRawAll(
        header,
        JwtOptions(signature = false, expiration = false, notBefore = false)
      )
      .flatMap {
        case (decodedHeader, contents, _) =>
          Json
            .decode(decodedHeader.getBytes)
            .to[JWTHeader]
            .valueEither
            .toTry
            .map((_, contents))
      }
}
