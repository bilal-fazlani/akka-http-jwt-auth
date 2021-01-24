package tech.bilal.akka.http.oidc.client

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import munit.FunSuite
import tech.bilal.akka.http.{ActorSystemMixin, KeycloakMixin}
import scala.concurrent.ExecutionContext.Implicits.global
import tech.bilal.akka.http.oidc.client.OIDCClient

class OIDCClientTest extends FunSuite with ActorSystemMixin() with KeycloakMixin() {
  test("can fetch oidc config") {
    implicit val system: ActorSystem[SpawnProtocol.Command] = actorSystem

    val client = OIDCClient(
      s"http://localhost:${keycloakSettings.port}/auth/realms/master/.well-known/openid-configuration"
    )

    client.fetchKeys.map { keySet =>
      assert(keySet.keys.nonEmpty)
    }
  }
}
