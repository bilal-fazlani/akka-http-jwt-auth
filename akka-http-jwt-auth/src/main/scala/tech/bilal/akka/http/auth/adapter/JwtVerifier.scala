package tech.bilal.akka.http.auth.adapter

import io.circe.Decoder
import pdi.jwt.{Jwt, JwtOptions}
import tech.bilal.akka.http.auth.adapter.crypto.Algorithm
import tech.bilal.akka.http.oidc.client.OIDCClient
import io.circe.parser.{decode, parse}

import java.security.PublicKey
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

class JwtVerifier(
    oidcClient: OIDCClient,
    publicKeyManager: PublicKeyManager,
    authConfig: AuthConfig
) {

  def verifyAndDecode[T: Decoder](tokenString: String): Future[Option[T]] = {
    val jwtOptions =
      JwtOptions(signature = true, expiration = true, notBefore = true)
    for {
      (header, jsonPayloadContents) <-
        Future.fromTry(getKIDAndContents(tokenString))
      keyMayBe <- publicKeyManager.getKey(header.kid)
      key = keyMayBe match {
        case Right(k) => k
        case Left(KeyError.KeyNotFound) =>
          throw RuntimeException(
            s"could not find key with kid: ${header.kid}"
          )
        case Left(KeyError.AuthServerDisconnected) =>
          throw RuntimeException(
            s"unabled to fetch keys from auth server"
          )
      }
      serverIssuer <- oidcClient.fetchOIDCConfig.map(_.issuer)
      algo: Try[Algorithm] =
        authConfig.supportedAlgorithms
          .find(_.toLowerCase == header.alg.toLowerCase)
          .map(Algorithm.apply)
          .getOrElse(
            throw RuntimeException(s"unsupported algorithm - ${header.alg}")
          )
      publicKey: PublicKey <-
        Future.fromTry(algo.flatMap(_.publicKey(key, header)))
      _ = Jwt.validate(tokenString, publicKey, jwtOptions)
      token <- Future.fromTry(decode[T](jsonPayloadContents).toTry)
      _ = if (authConfig.issuerCheck) {
        parse(jsonPayloadContents).toOption
          .flatMap(_.asObject)
          .flatMap(_.apply("iss"))
          .flatMap(_.asString) match {
          case Some(`serverIssuer`) => ()
          case Some(value) =>
            throw RuntimeException(
              s"issuer check failed. $value != $serverIssuer"
            )
          case None => throw RuntimeException("token does not have issuer")
        }
      }
    } yield Some(token)
  }

  private def getKIDAndContents(header: String): Try[(JWTHeader, String)] = {
    Jwt
      .decodeRawAll(
        header,
        JwtOptions(signature = false, expiration = false, notBefore = false)
      )
      .flatMap {
        case (decodedHeader, contents, _) =>
          decode[JWTHeader](decodedHeader)
            .map((_, contents))
            .toTry
      }
  }
}
