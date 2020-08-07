package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.auth.adapter.borer.decoderFor
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, get}
import akka.http.scaladsl.server.Route
import tech.bilal.akka.http.auth.adapter.AuthorizationPolicy.CustomPolicy
import tech.bilal.akka.http.auth.adapter.{
  SecurityDirectives,
  SyncAuthenticatorFactory,
  TokenDecoder
}

object Main extends App {
  val port = 9876
  implicit val actorSystem = ActorSystem(Behaviors.empty, "main")

  import actorSystem.executionContext

  val routes: Route = get {
    complete("OK")
  }

  case class AT(name: String, role: String)
  implicit val atDecoder: TokenDecoder[AT] = decoderFor[AT]
  val sec =
    new SecurityDirectives[AT](new SyncAuthenticatorFactory[AT], "master")

  import sec._

  val admin = sGet(CustomPolicy(_.role.toLowerCase == "admin"))

  val routes2 = admin { t =>
    complete(s"OK: $t")
  }

  Http()
    .newServerAt("0.0.0.0", port)
    .bind(routes)
    .map { _ =>
      println(s"server started at $port")
    }
    .recover {
      case x =>
        x.printStackTrace()
        actorSystem.terminate()
    }
}
