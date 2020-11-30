package tech.bilal.akka.http.auth.adapter.oidc

import java.util.Base64
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import io.bullet.borer.Json
import munit.FunSuite
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http.auth.adapter.AuthConfig
import tech.bilal.akka.http.oidc.client.models._
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}

class PublicKeyManagerTest extends FunSuite with Fixtures {

  private val fixture = FunFixture.map2(keycloak, actorSystem())

  fixture.test("can fetch keys") {
    case (_, actorSystem) =>
      implicit val system: ActorSystem[SpawnProtocol.Command] = actorSystem
      val client =  OIDCClient(
        s"http://localhost:${settings.port}/auth/realms/master/.well-known/openid-configuration"
      )
      val manager =  PublicKeyManager(client, 10.seconds)
      val tokenFromServer =
        BearerToken.fromServer(settings.port, "admin", "admin")
      val header = getHeader(tokenFromServer.token)
      val key = wait(manager.getKey(header.kid)).get
      assertEquals(key.kid, header.kid)
  }

  private def wait[T](f: Future[T]): T = Await.result(f, 5.seconds)

  private def getHeader(token: String) = {
    val headerString = token.split("\\.", 3).head
    val base64Decoded = Base64.getUrlDecoder.decode(headerString)
    Json.decode(base64Decoded).to[JWTHeader].value
  }
}
