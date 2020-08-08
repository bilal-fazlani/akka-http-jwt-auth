package tech.bilal.akka.http.auth.adapter.oidc

import org.tmt.embedded_keycloak.utils.BearerToken

object Main extends App {
  println(BearerToken.fromServer(8080, "admin", "admin"))
}
