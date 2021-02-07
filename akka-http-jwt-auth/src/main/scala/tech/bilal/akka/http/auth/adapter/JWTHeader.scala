package tech.bilal.akka.http.auth.adapter

import io.circe.Decoder

case class JWTHeader(
    //todo: is typ required?
    //todo: is any verification required on typ?
    typ: Option[String] = None,
    kid: String,
    alg: String
)

object JWTHeader {
  //todo: because macro is not being used, null values for typ may not work 
  given Decoder[JWTHeader] = Decoder.forProduct3("typ", "kid", "alg")(JWTHeader.apply)
}
