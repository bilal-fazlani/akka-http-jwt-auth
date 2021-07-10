package example

import org.tmt.embedded_keycloak.utils.BearerToken

object GetToken extends App {
  val token = BearerToken.fromServer(8080, "admin", "admin")
  println(s"use this to test /secure endpoint")
  println(
    s"http GET http://localhost:9876/secure 'Authorization:Bearer ${token.token}'"
  )
}
