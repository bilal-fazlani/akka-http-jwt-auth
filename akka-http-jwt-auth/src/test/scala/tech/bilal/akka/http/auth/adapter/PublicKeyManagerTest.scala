package tech.bilal.akka.http.auth.adapter

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.circe.parser.decode
import munit.FunSuite
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http._
import tech.bilal.akka.http.client.circe.HttpClient
import tech.bilal.akka.http.oidc.client.models._
import tech.bilal.akka.http.oidc.client.OIDCClient

import java.util.Base64
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class PublicKeyManagerTest
    extends FunSuite
    with KeycloakMixin()
    with ActorSystemMixin() {

  test("can fetch keys") {
    given system: ActorSystem[SpawnProtocol.Command] = actorSystem
    val authUrl = s"http://localhost:${keycloakSettings.port}/auth/realms/master/.well-known/openid-configuration"
    val client = OIDCClient(authUrl)
    val authConfig = AuthConfig("master", authUrl)
    val manager = PublicKeyManager(client, authConfig)
    val tokenFromServer =
      BearerToken.fromServer(keycloakSettings.port, "admin", "admin")
    val header = getHeader(tokenFromServer.token)
    val key = manager.getKey(header.kid).block.toOption.get
    assertEquals(key.kid, header.kid)
  }

  private def getHeader(token: String): JWTHeader = {
    val headerString = token.split("\\.", 3).head
    val base64Decoded = Base64.getUrlDecoder.decode(headerString)
    decode[JWTHeader](String(base64Decoded)).toTry.get
  }
}
