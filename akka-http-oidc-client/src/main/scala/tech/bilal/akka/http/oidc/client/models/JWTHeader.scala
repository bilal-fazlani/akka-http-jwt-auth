package tech.bilal.akka.http.oidc.client.models

import io.bullet.borer.Decoder

case class JWTHeader(
    typ: Option[String] = None, 
    //todo: is typ required?
    //todo: is any verification required on typ?
    kid: String,
    alg: String //todo: library should support a limited number of algs
)

object JWTHeader {
  import io.bullet.borer.NullOptions._
  //todo: because macro is not being used, null values for typ may not work 
  given dec as Decoder[JWTHeader] = Decoder.from(JWTHeader.apply _)
}
