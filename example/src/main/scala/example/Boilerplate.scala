package example

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.bullet.borer.Decoder
import tech.bilal.akka.http.auth.adapter.{AsyncAuthenticatorFactory, AuthDirectives, JwtVerifier}
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

trait Boilerplate {
  val port = 9876
  
  given actorSystem as ActorSystem[SpawnProtocol.Command] = ActorSystem(SpawnProtocol(), "main")
  
  given ec as ExecutionContext = actorSystem.executionContext
  
  case class AT(preferred_username: String)

  given dec as Decoder[AT] = Decoder.from(AT.apply _)
  
  val authUrl =
    s"http://localhost:8080/auth/realms/master/.well-known/openid-configuration"
  private val oIDCClient = OIDCClient(authUrl)
  val authDirectives =
    AuthDirectives[AT](
      new AsyncAuthenticatorFactory[AT](
        new JwtVerifier(oIDCClient, PublicKeyManager(oIDCClient, 24.hours))
      ),
      "master"
    )
}
