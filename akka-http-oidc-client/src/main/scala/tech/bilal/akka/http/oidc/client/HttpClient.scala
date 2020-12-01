package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.MediaTypes
import akka.stream.Materializer
import de.heikoseeberger.akkahttpjackson.JacksonSupport
import scala.reflect.runtime.universe._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class HttpClient(using system: ClassicActorSystemProvider)
    extends JacksonSupport {
  def get[O: TypeTag](url: String)(using ec: ExecutionContext): Future[O] = {
    Http()
      .singleRequest(Get(url))
      .transform {
        case Success(value) if value.status.isFailure() =>
          Failure(
             RuntimeException(
              s"call to $url failed with status code ${value.status.intValue()}"
            )
          )
        case x => x
      }(ec)
      .flatMap(res => unmarshaller.apply(res.entity)(ec, Materializer(system)))(ec)
  }
}
