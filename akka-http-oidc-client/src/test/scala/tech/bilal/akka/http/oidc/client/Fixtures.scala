package tech.bilal.akka.http.oidc.client

import akka.actor.typed.SpawnProtocol.Command
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import org.tmt.embedded_keycloak.KeycloakData.{AdminUser, Realm}
import org.tmt.embedded_keycloak.impl.StopHandle
import org.tmt.embedded_keycloak.{EmbeddedKeycloak, KeycloakData, Settings}
import munit.FunSuite
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

trait Fixtures extends FunSuite {
  protected val settings: Settings = Settings(printProcessLogs = false)
  protected val keycloakData: KeycloakData = KeycloakData(
    AdminUser("admin", "admin"),
    Set(
      Realm(
        "test-realm",
        Set("admin")
      )
    )
  )

  val keycloak: FunFixture[StopHandle] =
    FunFixture[StopHandle](
      _ =>
        Await.result(
          EmbeddedKeycloak(keycloakData, settings).startServer(),
          120.seconds
        ),
      s => s.stop()
    )

  def actorSystem(
                   name: String = "test-system"
                 ): FunFixture[ActorSystem[Command]] =
    FunFixture[ActorSystem[Command]](
      _ => {
        ActorSystem(SpawnProtocol(), name)
      },
      f => {
        f.classicSystem.terminate()
      }
    )
}
