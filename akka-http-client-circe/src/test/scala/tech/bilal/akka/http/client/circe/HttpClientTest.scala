package tech.bilal.akka.http.client.circe

import tech.bilal.akka.http.oidc.client.HttpClient
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.actor.typed.scaladsl._
import io.circe.Decoder
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

case class Result(userId:Int, completed:Boolean, id:Int, title: String)

object TA extends App {
    given as:ActorSystem[_] = ActorSystem(Behaviors.empty, "as")
    import as.executionContext
    val hc = HttpClient()
    given Decoder[Result] = Decoder.forProduct4("userId", "completed", "id", "title")(Result.apply)
    val r = Await.result(hc.get[Result]("https://jsonplaceholder.typicode.com/todos/2"), 5.seconds)
    println(r)
    as.terminate
}