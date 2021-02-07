package tech.bilal.akka.http.client.circe

import akka.http.scaladsl.model.MediaTypes.`application/json`
import akka.http.scaladsl.model.{ContentTypeRange, HttpEntity, MediaRanges, MediaType, ResponseEntity}
import akka.http.scaladsl.unmarshalling.{FromEntityUnmarshaller, Unmarshaller}
import akka.util.ByteString
import io.circe.{Decoder, jawn}
import io.circe.parser.decode

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait CirceMarshallingSupport {
  def mediaTypes: Seq[MediaType.WithFixedCharset] =
    List(`application/json`)
  
  def unmarshallerContentTypes: Seq[ContentTypeRange] =
      mediaTypes.map(ContentTypeRange.apply)
  
  given unm[T:Decoder](using ExecutionContext) : FromEntityUnmarshaller[T] = {
    Unmarshaller.byteStringUnmarshaller
      .forContentTypes(unmarshallerContentTypes: _*)
      .map{
        case ByteString.empty => throw Unmarshaller.NoContentException
        case data             => decode[T](data.utf8String).toTry.get
      }
  }
}
