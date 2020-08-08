package tech.bilal.akka.http.auth.adapter.oidc

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import munit.FunSuite
import org.tmt.embedded_keycloak.utils.BearerToken
import tech.bilal.akka.http.auth.adapter.JwtVerifier

import scala.concurrent.duration.DurationInt

class JwtVerifierTest extends FunSuite with Fixtures {

  private val fixture = FunFixture.map2(keycloak, actorSystem())

  fixture.test("can verify token") {
    case (_, actorSystem) =>
      implicit val system: ActorSystem[SpawnProtocol.Command] = actorSystem
      val client = new OIDCClient(
        s"http://localhost:${settings.port}/auth/realms/master/.well-known/openid-configuration"
      )
      println(BearerToken.fromServer(settings.port, "admin", "admin"))
      val manager = new PublicKeyManager(client, 10.seconds)
      val verifier = new JwtVerifier(manager)
      val token = BearerToken.fromServer(settings.port, "admin", "admin")
      verifier.verifyAndDecode[Int](token.token)
  }
}
