package tech.bilal.akka.http.oidc.client

import akka.actor.ClassicActorSystemProvider
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.SpawnProtocol.Command
import akka.actor.typed.{ActorRef, ActorSystem, Scheduler, SpawnProtocol}
import akka.util.Timeout
import tech.bilal.akka.http.ActorTestKitMixin
import tech.bilal.akka.http._

import java.net.ConnectException
import scala.concurrent.{ExecutionContext, Future, Promise, TimeoutException}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success, Try}

class LazySuccessCachedFutureTest extends munit.FunSuite with ActorTestKitMixin {
  test("get should a future when get") {
    given ActorSystem[_] = actorTestKit.internalSystem
    
    given Scheduler = actorTestKit.internalSystem.scheduler
    given ExecutionContext = actorTestKit.internalSystem.executionContext
    given ActorRef[Command] = actorTestKit.spawn(SpawnProtocol())
    
    var called = 0
    val cachedFuture = new LazySuccessCachedFuture[Int](Future.successful{
      called += 1
      called
    })
    
    val result1 = {
      given t:Timeout = Timeout(5.seconds)
      cachedFuture.future(t).block
    }
    assertEquals(result1, 1)
    assertEquals(called, 1)
  }

  test("get should a same future when get call multiple times") {
    given ActorSystem[_] = actorTestKit.internalSystem

    given Scheduler = actorTestKit.internalSystem.scheduler
    given ExecutionContext = actorTestKit.internalSystem.executionContext
    given ActorRef[Command] = actorTestKit.spawn(SpawnProtocol())
    
    var called = 0

    def future: Future[Int] = delayedFuture(2.seconds) {
      called += 1
      Success(called)
    }
    
    val cachedFuture = new LazySuccessCachedFuture[Int](future)
    given t:Timeout = Timeout(5.seconds)
    val result1 = cachedFuture.future(t).block
    assertEquals(result1, 1)
    assertEquals(called, 1)

    val result2 = cachedFuture.future(t).block(5.seconds)
    assertEquals(result2, 1)
    assertEquals(called, 1)

    val result3 = cachedFuture.future(t).block(5.seconds)
    assertEquals(result3, 1)
    assertEquals(called, 1)

    val result4 = cachedFuture.future(t).block(5.seconds)
    assertEquals(result4, 1)
    assertEquals(called, 1)
  }

  test("get should retry a failed future") {
    given ActorSystem[_] = actorTestKit.internalSystem

    given Scheduler = actorTestKit.internalSystem.scheduler
    given ExecutionContext = actorTestKit.internalSystem.executionContext
    given ActorRef[Command] = actorTestKit.spawn(SpawnProtocol())

    var called = 0

    def future: Future[Int] = delayedFuture(2.seconds) {
      called += 1
      if(called <= 1) Failure(new IllegalStateException("service unavailable"))
      else Success(called)
    }

    val cachedFuture = new LazySuccessCachedFuture[Int](future)
    given t:Timeout = Timeout(5.seconds)
    
    interceptMessage[IllegalStateException]("service unavailable")(cachedFuture.future(t).block)
    assertEquals(called, 1)

    val result2 = cachedFuture.future(t).block
    assertEquals(result2, 2)
    assertEquals(called, 2)

    val result3 = cachedFuture.future(t).block
    assertEquals(result3, 2)
    assertEquals(called, 2)

    val result4 = cachedFuture.future(t).block
    assertEquals(result4, 2)
    assertEquals(called, 2)
  }

  test("when a future keeps failing before timeout") {
    given ActorSystem[_] = actorTestKit.internalSystem

    given Scheduler = actorTestKit.internalSystem.scheduler
    given ExecutionContext = actorTestKit.internalSystem.executionContext
    given ActorRef[Command] = actorTestKit.spawn(SpawnProtocol())

    var called = 0

    def future: Future[Int] = delayedFuture(4.seconds) {
      called += 1
      Failure(new IllegalStateException("service unavailable"))
    }

    val cachedFuture = new LazySuccessCachedFuture[Int](future)
    given t:Timeout = Timeout(5.seconds)

    interceptMessage[IllegalStateException]("service unavailable")(cachedFuture.future(t).block)
    assertEquals(called, 1)

    interceptMessage[IllegalStateException]("service unavailable")(cachedFuture.future(t).block)
    assertEquals(called, 2)

    interceptMessage[IllegalStateException]("service unavailable")(cachedFuture.future(t).block)
    assertEquals(called, 3)

    interceptMessage[IllegalStateException]("service unavailable")(cachedFuture.future(t).block)
    assertEquals(called, 4)
  }

  test("when a failing future takes more time than timeout") {
    given ActorSystem[_] = actorTestKit.internalSystem

    given Scheduler = actorTestKit.internalSystem.scheduler
    given ExecutionContext = actorTestKit.internalSystem.executionContext
    given ActorRef[Command] = actorTestKit.spawn(SpawnProtocol())

    var called = 0

    def future: Future[Int] = delayedFuture(5.seconds) {
      called += 1
      Failure(new ConnectException("connection closed"))
    }

    val cachedFuture = new LazySuccessCachedFuture[Int](future)
    given t:Timeout = Timeout(4.seconds)

    intercept[TimeoutException](cachedFuture.future(t).block)
    interceptMessage[ConnectException]("connection closed")(cachedFuture.future(t).block)
    intercept[TimeoutException](cachedFuture.future(t).block)
    interceptMessage[ConnectException]("connection closed")(cachedFuture.future(t).block)
    intercept[TimeoutException](cachedFuture.future(t).block)
  }
  
  private def delayedFuture[T](delay:FiniteDuration)(value: => Try[T])(using scheduler:Scheduler, ec:ExecutionContext):Future[T] = {
    val promise = Promise[T]()
    scheduler.scheduleOnce(delay, () => promise.complete(value))
    promise.future
  }
}
