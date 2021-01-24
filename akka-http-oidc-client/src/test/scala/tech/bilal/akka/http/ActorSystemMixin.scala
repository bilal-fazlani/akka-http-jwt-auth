package tech.bilal.akka.http

import akka.actor.typed.{ActorSystem, SpawnProtocol}
import munit.FunSuite

trait ActorSystemMixin(name: String = "test") extends FunSuite {
  var actorSystem: ActorSystem[SpawnProtocol.Command] = null
  override def beforeAll() = {
    super.beforeAll()
    actorSystem = ActorSystem(SpawnProtocol(), name)
  }

  override def afterAll() = {
    actorSystem.terminate()
    super.afterAll()
  }
}
