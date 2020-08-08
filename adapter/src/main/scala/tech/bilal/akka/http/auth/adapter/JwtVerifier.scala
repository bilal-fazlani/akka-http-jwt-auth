package tech.bilal.akka.http.auth.adapter

import io.bullet.borer.Decoder
import pdi.jwt.{Jwt, JwtOptions}

class JwtVerifier() {
  def verifyAndDecode[T: Decoder](str: String): Option[T] = ???

  def verify(str: String): Boolean = {
    val a = Jwt.decode(str)
    Jwt.isValid(
      token = str,
      options = JwtOptions(
        signature = true,
//        expiration = true,
//        notBefore = true,
        leeway = 1
      )
    )
  }
}
