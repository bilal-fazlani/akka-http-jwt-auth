package example

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
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
    .recover { case x =>
      x.printStackTrace()
      actorSystem.terminate()
    }
}
