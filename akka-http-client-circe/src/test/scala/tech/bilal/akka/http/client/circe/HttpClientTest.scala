package tech.bilal.akka.http.client.circe

import tech.bilal.akka.http.client.circe.HttpClient
import akka.actor.typed.{ActorSystem, SpawnProtocol}
import akka.actor.typed.scaladsl._
import akka.http.scaladsl.client.RequestBuilding.Get
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader, HttpResponse, ResponseEntity}
import akka.http.scaladsl.model.headers.`Content-Type`
import io.circe.Decoder
import tech.bilal.akka.http._
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class HttpClientTest extends munit.FunSuite with ActorSystemMixin() {
  test("should get and deserialize json from web api"){
    given system: ActorSystem[SpawnProtocol.Command] = actorSystem
    import system.executionContext
    val client = new HttpClient()
    val dto:Dto = client.get[Dto]("https://jsonplaceholder.typicode.com/todos/1").block
    assertEquals(dto, Dto(1,false,1,"delectus aut autem"))
  }

  test("should fail when 404 occurs"){
    interceptMessage[RuntimeException]("call to https://jsonplaceholder.typicode.com/todos/404 failed with status code 404"){
      given system: ActorSystem[SpawnProtocol.Command] = actorSystem
      import system.executionContext
      val client = new HttpClient()
      val dto:Dto = client.get[Dto]("https://jsonplaceholder.typicode.com/todos/404").block 
    }
  }
}