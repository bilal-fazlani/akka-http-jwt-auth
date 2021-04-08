package tech.bilal.akka.http.oidc.client

import akka.actor
import akka.actor.ClassicActorSystemProvider
import akka.actor.typed.SpawnProtocol.{Command, Spawn}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter.ClassicActorSystemOps
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, BehaviorInterceptor, Props, Scheduler, TypedActorContext}
import akka.util.Timeout
import tech.bilal.akka.http.oidc.client.models.Key

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import scala.annotation.tailrec
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

class LazySuccessCachedFuture[A](f: => Future[A])(
  using spawner: ActorRef[Command], 
  scheduler:Scheduler,
  ec:ExecutionContext
) {
  def future(timeout:Timeout):Future[A] = for {
    ar: ActorRef[LazySuccessCacheMessage] <- lazySuccessCachedFutureActor
    valueMaybe:Try[A] <- {
      given Timeout = timeout
      ar ? Get.apply
    }
    value <- Future.fromTry(valueMaybe)
  } yield value

  private lazy val lazySuccessCachedFutureActor: Future[ActorRef[LazySuccessCacheMessage]] =
    given timeout:Timeout = Timeout(2.seconds)
    spawner ? (Spawn[LazySuccessCacheMessage](
    empty,
    null, //spawns anonymous
    Props.empty,
    _
    )
  )
  
  private sealed trait LazySuccessCacheMessage
  private case class Get(replyTo: ActorRef[Try[A]]) extends LazySuccessCacheMessage
  private case class Set(value: Try[A]) extends LazySuccessCacheMessage
  private case object Fetch extends LazySuccessCacheMessage

  private def empty: Behavior[LazySuccessCacheMessage] = {
    Behaviors.setup { ctx =>
      Behaviors.receiveMessagePartial {
        case Get(replyTo) =>
          ctx.self ! Fetch
          loading(Seq(replyTo))
        case _ =>
          Behaviors.same
      }
    }
  }

  private def loading(listeners: Seq[ActorRef[Try[A]]], print:Boolean = true): Behavior[LazySuccessCacheMessage] =
    Behaviors.setup { ctx =>
      Behaviors.receiveMessagePartial {
        case Fetch =>
          ctx.pipeToSelf(f)(Set.apply)
          Behaviors.same
        case Get(replyTo) =>
          loading(listeners.appended(replyTo), false)
        case Set(Success(value)) =>
          listeners.foreach(_ ! Success(value))
          full(value)
        case Set(Failure(err)) =>
          listeners.foreach(_ ! Failure(err))
          empty
      }
    }

  private def full(state: A): Behavior[LazySuccessCacheMessage] =
    Behaviors.receiveMessagePartial {
      case Get(replyTo) =>
        replyTo ! Success(state)
        Behaviors.same
      case _ =>
        Behaviors.same
    }
}