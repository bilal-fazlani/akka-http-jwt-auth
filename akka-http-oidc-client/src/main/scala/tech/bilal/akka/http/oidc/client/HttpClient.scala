package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.MediaTypes
import io.bullet.borer.Decoder
import io.bullet.borer.compat.AkkaHttpCompat

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpClient(using system: ClassicActorSystemProvider)
    extends AkkaHttpCompat {
  def get[O: Decoder](url: String)(using ec: ExecutionContext): Future[O] = {
    val unmarshaller =
      borerUnmarshaller[O](jsonMediaType = MediaTypes.`application/json`)
    Http()
      .singleRequest(Get(url))
      .transform {
        case Success(value) if value.status.isFailure() =>
          Failure(
            new RuntimeException(
              s"call to $url failed with status code ${value.status.intValue()}"
            )
          )
        case x => x
      }
      .flatMap(res => unmarshaller(res.entity))
  }
}
