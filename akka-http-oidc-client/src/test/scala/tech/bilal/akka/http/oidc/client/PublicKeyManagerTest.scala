package tech.bilal.akka.http.oidc.client

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http.oidc.client.models._
import tech.bilal.akka.http.oidc.client.{OIDCClient, PublicKeyManager}
import munit.FunSuite
import java.util.Base64
import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Future}
import io.circe.parser.decode

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

  private def getHeader(token: String): JWTHeader = {
    val headerString = token.split("\\.", 3).head
    val base64Decoded = Base64.getUrlDecoder.decode(headerString)
    decode[JWTHeader](String(base64Decoded)).toTry.get
  }
}
