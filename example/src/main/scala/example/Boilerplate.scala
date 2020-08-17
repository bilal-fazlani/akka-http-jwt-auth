package example

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.bullet.borer.Decoder
import io.bullet.borer.derivation.MapBasedCodecs
import tech.bilal.akka.http.auth.adapter.{
  AsyncAuthenticatorFactory,
  AuthDirectives,
  JwtVerifier
}
import tech.bilal.akka.http.auth.adapter.oidc.{OIDCClient, PublicKeyManager}

import scala.concurrent.duration.DurationInt

trait Boilerplate {
  val port = 9876
  implicit val actorSystem: ActorSystem[SpawnProtocol.Command] =
    ActorSystem(SpawnProtocol(), "main")

  import actorSystem.executionContext

  case class AT(preferred_username: String)
  implicit val dec: Decoder[AT] = MapBasedCodecs.deriveDecoder[AT]
  val authUrl =
    s"http://localhost:8080/auth/realms/master/.well-known/openid-configuration"
  private val oIDCClient = new OIDCClient(authUrl)
  val authDirectives =
    new AuthDirectives[AT](
      new AsyncAuthenticatorFactory[AT](
        new JwtVerifier(oIDCClient, new PublicKeyManager(oIDCClient, 24.hours))
      ),
      "master"
    )
}
