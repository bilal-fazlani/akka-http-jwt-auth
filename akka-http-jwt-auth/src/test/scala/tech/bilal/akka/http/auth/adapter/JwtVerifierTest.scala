package tech.bilal.akka.http.auth.adapter

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.circe.Decoder
import munit.FunSuite
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http.auth.adapter.{AuthConfig, JwtVerifier}
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class JwtVerifierTest extends FunSuite with Fixtures {
  
  private val fixture = FunFixture.map2(keycloak, actorSystem())

  case class TestToken(
      preferred_username: String,
      scope: String,
      sub: String,
      iss: String
  )
  given Decoder[TestToken] = Decoder
    .forProduct4("preferred_username", "scope", "sub", "iss")(TestToken.apply)
   
  fixture.test("can verify token") {
    case (_, actorSystem) =>
      implicit val system: ActorSystem[SpawnProtocol.Command] = actorSystem
      val client =  OIDCClient(
        s"http://localhost:${settings.port}/auth/realms/master/.well-known/openid-configuration"
      )
      println(BearerToken.fromServer(settings.port, "admin", "admin"))
      val manager =  PublicKeyManager(client, 10.seconds)
      val verifier =  JwtVerifier(client, manager, AuthConfig(
        s"http://localhost:${settings.port}/auth/realms/master",
        Set("RS256")
      ))
      val token = BearerToken.fromServer(settings.port, "admin", "admin")
      val decoded = Await
        .result(verifier.verifyAndDecode[TestToken](token.token), 10.seconds)
        .get
      assertEquals(decoded.preferred_username, "admin")
  }
}