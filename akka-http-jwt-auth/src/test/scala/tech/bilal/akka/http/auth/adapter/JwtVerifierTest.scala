package tech.bilal.akka.http.auth.adapter

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.circe.Decoder
import munit.FunSuite
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http._
import tech.bilal.akka.http.auth.adapter.{AuthConfig, JwtVerifier}
import tech.bilal.akka.http.oidc.client.OIDCClient
import tech.bilal.akka.http.client.circe.HttpClient
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class JwtVerifierTest extends FunSuite with ActorSystemMixin() with KeycloakMixin() {
  
  case class TestToken(
      preferred_username: String,
      scope: String,
      sub: String,
      iss: String
  )
  given Decoder[TestToken] = Decoder
    .forProduct4("preferred_username", "scope", "sub", "iss")(TestToken.apply)
  
  test("can verify token") {
    implicit val system: ActorSystem[SpawnProtocol.Command] = actorSystem
    val authUrl = s"http://localhost:${keycloakSettings.port}/auth/realms/master/.well-known/openid-configuration"
    val client =  OIDCClient(
      authUrl,
      HttpClient()
    )
    val authConfig = AuthConfig("master", authUrl)
    val manager =  PublicKeyManager(client, authConfig)
    val verifier =  JwtVerifier(client.oidcConfig, manager, authConfig)
    val token = BearerToken.fromServer(keycloakSettings.port, "admin", "admin")
    val decoded = Await
      .result(verifier.verifyAndDecode[TestToken](token.token), 10.seconds)
      .get
    assertEquals(decoded.preferred_username, "admin")
  }
}
