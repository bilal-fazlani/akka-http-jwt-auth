package tech.bilal.akka.http.auth.adapter

import io.bullet.borer.{Decoder, Json}
import pdi.jwt.{Jwt, JwtOptions}
import tech.bilal.akka.http.auth.adapter.crypto.Algorithm
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}
import tech.bilal.akka.http.oidc.client.models.JWTHeader

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class JwtVerifier(oidcClient: OIDCClient, publicKeyManager: PublicKeyManager, authConfig: AuthConfig) {
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
      algo = authConfig.supportedAlgorithms.find(_.toLowerCase == header.alg.toLowerCase)
        .map(Algorithm.apply)
        .getOrElse(throw RuntimeException(s"unsupported algorithm - ${header.alg}"))
      publicKey <- Future.fromTry(algo.flatMap(_.publicKey(key, header)))
      _ = Jwt.validate(tokenString, publicKey, jwtOptions)
      token <- Future.fromTry(Json.decode(jsonPayloadContents.getBytes).to[T].valueTry)
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
