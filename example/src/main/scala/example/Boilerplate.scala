package example

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.circe.Decoder
import tech.bilal.akka.http.auth.adapter.{AsyncAuthenticatorFactory, AuthConfig, AuthDirectives, JwtVerifier}
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait Boilerplate {
  val port = 9876

  given actorSystem: ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "main")

  given ExecutionContext = actorSystem.executionContext

  case class AT(preferred_username: String)
  
  given Decoder[AT] = Decoder.forProduct1("preferred_username")(AT.apply)

  val authUrl =
    s"http://localhost:8080/auth/realms/master/.well-known/openid-configuration"
  private val oIDCClient = OIDCClient(authUrl)
  val authDirectives =
    AuthDirectives[AT](
      AsyncAuthenticatorFactory[AT](
        JwtVerifier(oIDCClient, PublicKeyManager(oIDCClient, 24.hours), AuthConfig("http://localhost:8080/auth/realms/master", Set("RS256")))
      ),
      "master"
    )
}
