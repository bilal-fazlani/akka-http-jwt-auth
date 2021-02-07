package example

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.tmt.embedded_keycloak.utils.BearerToken

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends App with Boilerplate {

  import authDirectives._

  val adminPolicy = policy(_.preferred_username == "admin")

  val routes: Route = get {
    path("secure") {
      adminPolicy {
        complete("Safe OK")
      }
    } ~ pathEndOrSingleSlash {
      complete("OK")
    }
  }

  Http()
    .newServerAt("0.0.0.0", port)
    .bind(routes)
    .map { _ =>
      println(s"server started at $port")
    }
//    .map { _ =>
//      val token = BearerToken.fromServer(8080, "admin", "admin")
//      println(s"use this to test /secure endpoint")
//      println(s"http GET http://localhost:$port/secure 'Authorization:Bearer ${token.token}'")
//    }
    .recover { case x =>
      x.printStackTrace()
      actorSystem.terminate()
    }
}
