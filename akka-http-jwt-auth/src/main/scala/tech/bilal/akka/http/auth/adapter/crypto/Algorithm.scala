package tech.bilal.akka.http.auth.adapter.crypto

import java.math.BigInteger
import java.security.spec.RSAPublicKeySpec
import java.security.{KeyFactory, PublicKey}
import java.util.Base64
import tech.bilal.akka.http.oidc.client.models._

import scala.util.{Failure, Success, Try}

sealed trait Algorithm {
  def publicKey(key: Key, jwtHeader: JWTHeader): Try[PublicKey]
}
object Algorithm {
  def apply(algorithm: String): Try[Algorithm] =
    algorithm.toUpperCase match {
      case "RS256" => Success(RSA)
      case x       => Failure(RuntimeException(s"Unsupported algorithm: $x"))
    } //todo: support more algorithms

  private val base64Decoder = Base64.getUrlDecoder

  object RSA extends Algorithm {
    override def publicKey(key: Key, jwtHeader: JWTHeader): Try[PublicKey] = {
      Try {
        val kf: KeyFactory = KeyFactory.getInstance(key.kty)
        val modulus = BigInteger(1, base64Decoder.decode(key.n))
        val exponent = BigInteger(1, base64Decoder.decode(key.e))
        val spec = RSAPublicKeySpec(modulus, exponent)
        kf.generatePublic(spec)
      }
    }
  }
}
