package example

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs
import tech.bilal.akka.http.auth.adapter.oidc.{OIDCClient, PublicKeyManager}
import tech.bilal.akka.http.auth.adapter.{
  AsyncAuthenticatorFactory,
  AuthDirectives,
  JwtVerifier
}

import scala.concurrent.duration.DurationInt

object Main extends App {
  val port = 9876
  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "main")

  import actorSystem.executionContext

  case class AT(preferred_username: String)
  implicit val dec: Decoder[AT] = MapBasedCodecs.deriveDecoder[AT]
  val authUrl =
    s"http://localhost:8080/auth/realms/master/.well-known/openid-configuration"
  private val oIDCClient = new OIDCClient(authUrl)
  val sec =
    new AuthDirectives[AT](
      new AsyncAuthenticatorFactory[AT](
        new JwtVerifier(oIDCClient, new PublicKeyManager(oIDCClient, 24.hours))
      ),
      "master"
    )

  import sec._

  val adminPolicy = policy(_.preferred_username == "admin")

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
