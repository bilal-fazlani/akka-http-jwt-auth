package tech.bilal.akka.http

import akka.actor.typed.{ActorRef, ActorSystem, SpawnProtocol}
import munit.FunSuite
import akka.actor.testkit.typed.scaladsl.*
import akka.actor.typed.SpawnProtocol.Command

trait ActorTestKitMixin extends FunSuite {
  var actorTestKit: ActorTestKit = null

  override def beforeAll() = {
    super.beforeAll()
    actorTestKit = ActorTestKit()
    actorTestKit.internalSystem
  }

  override def afterAll() = {
    actorTestKit.shutdownTestKit()
    super.afterAll()
  }
}
