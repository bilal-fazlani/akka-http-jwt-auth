package example

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.auth.adapter.borer._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs
import tech.bilal.akka.http.auth.adapter.{
  AuthDirectives,
  JwtVerifier,
  SyncAuthenticatorFactory
}

object Main extends App {
  val port = 9876
  implicit val actorSystem: ActorSystem[Nothing] =
    ActorSystem(Behaviors.empty, "main")

  import actorSystem.executionContext

  case class AT(name: String, role: String)
  implicit val dec: Decoder[AT] = MapBasedCodecs.deriveDecoder[AT]
  val sec =
    new AuthDirectives[AT](
      new SyncAuthenticatorFactory[AT](new JwtVerifier),
      "master"
    )

  import sec._

  val adminPolicy = policy(_.role.toLowerCase == "admin")

  val routes: Route = get {
    path("secure") {
      adminPolicy {
        complete("Safe OK")
      }
    } ~ pathEndOrSingleSlash {
      complete("OK")
    }
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
