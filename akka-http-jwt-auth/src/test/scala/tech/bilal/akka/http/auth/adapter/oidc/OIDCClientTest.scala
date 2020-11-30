package tech.bilal.akka.http.auth.adapter.oidc

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import munit.FunSuite
import tech.bilal.akka.http.oidc.client.OIDCClient
import scala.concurrent.ExecutionContext.Implicits.global

class OIDCClientTest extends FunSuite with Fixtures {

  private val fixture = FunFixture.map2(keycloak, actorSystem())

  fixture.test("can fetch oidc config") {
    case (_, provider) =>
      implicit val system: ActorSystem[SpawnProtocol.Command] = provider

      val client =  OIDCClient(
        s"http://localhost:${settings.port}/auth/realms/master/.well-known/openid-configuration"
      )

      client.fetchKeys.map { keySet =>
        assert(keySet.keys.nonEmpty)
      }
  }
}
