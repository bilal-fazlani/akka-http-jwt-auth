package tech.bilal.akka.http.auth.adapter.oidc

import munit.FunSuite

import scala.concurrent.ExecutionContext.Implicits.global

class OIDCClientTest extends FunSuite with Fixtures {

  private val fixture = FunFixture.map2(keycloak, actorSystem())

  fixture.test("can fetch oidc config") {
    case (_, provider) =>
      implicit val system = provider

      val client = new OIDCClient(
        s"http://localhost:${settings.port}/auth/realms/master/.well-known/openid-configuration"
      )

      client.fetchKeys.map(println)
  }
}
