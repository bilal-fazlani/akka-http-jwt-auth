package tech.bilal.akka.http.client.circe

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import tech.bilal.akka.http.client.circe.CirceMarshallingSupport

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import io.circe.Decoder

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.FiniteDuration

class HttpClient(using system: ClassicActorSystemProvider) extends CirceMarshallingSupport { 
  private def durationFrom(from:LocalDateTime) =
    FiniteDuration.apply(ChronoUnit.SECONDS.between(from, LocalDateTime.now()), TimeUnit.SECONDS)
  
  def get[O: Decoder](url: String)(using ec: ExecutionContext) : Future[O] = {
    Http()
      .singleRequest(Get(url))
      .transform {
        case Success(value) if value.status.isFailure() =>
          value.discardEntityBytes()
          Failure(
             RuntimeException(
              s"call to $url failed with status code ${value.status.intValue()}"
            )
          )
        case x => x
      }(ec)
      .flatMap(res => Unmarshal(res.entity).to[O])
  }
}
