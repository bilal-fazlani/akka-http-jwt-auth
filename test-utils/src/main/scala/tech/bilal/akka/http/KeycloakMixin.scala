package tech.bilal.akka.http

import org.tmt.embedded_keycloak._
import munit.FunSuite
import org.tmt.embedded_keycloak.KeycloakData._
import org.tmt.embedded_keycloak.impl.StopHandle
import tech.bilal.akka.http._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

trait KeycloakMixin(keycloakData: Option[KeycloakData] = None)
  extends FunSuite {
  var stopHandle: StopHandle = null
  protected val keycloakSettings: Settings = Settings(printProcessLogs = false)
  val keycloak: EmbeddedKeycloak = EmbeddedKeycloak(
    keycloakData.getOrElse(
      KeycloakData(
        AdminUser("admin", "admin"),
        Set(
          Realm(
            "test-realm",
            Set("admin")
          )
        )
      )
    ),
    keycloakSettings
  )

  override def beforeAll() = {
    super.beforeAll()
    stopHandle = keycloak.startServer().block(120.seconds)
  }

  override def afterAll() = {
    stopHandle.stop()
    super.afterAll()
  }
}
