//package akka.http.auth.adapter
//
//import io.bullet.borer.{Decoder, Json}
//import tech.bilal.akka.http.auth.adapter.JsonDecoder
//
//import scala.language.implicitConversions
//
////package object borer {
////  implicit def fromBorer[T: Decoder](dec: Decoder[T]): JsonDecoder = new JsonDecoder {
////    override def decode[T](str: String): Option[T] = ???
////  }
//////    (string: String) =>
//////      Json.decode(string.getBytes).to[T].valueEither.toOption
////}
