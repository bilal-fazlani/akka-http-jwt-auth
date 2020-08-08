package tech.bilal.akka.http.auth.adapter.oidc

import akka.actor.typed
import akka.actor.typed.SpawnProtocol.{Command, Spawn}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.{Failure, Success}

class PublicKeyManager(
    oidcClient: OIDCClient,
    keyRefreshInterval: FiniteDuration
)(implicit
    actorSystem: ActorSystem[Command]
) {

  sealed trait PublicKeyMessage
  private case class GetKeys(replyTo: ActorRef[KeySet]) extends PublicKeyMessage
  private case class SetKeys(keys: KeySet) extends PublicKeyMessage
  private case object FetchKeys extends PublicKeyMessage

  object PublicKeyManagerActor {
    def init: Behavior[PublicKeyMessage] =
      Behaviors.setup[PublicKeyMessage](ctx =>
        Behaviors.withStash[PublicKeyMessage](1024) { st =>
          ctx.pipeToSelf(oidcClient.fetchKeys) {
            case Success(value) => SetKeys(value)
            case Failure(exception) =>
              throw exception //todo: what should happen when init fails
          }
          Behaviors.receiveMessage[PublicKeyMessage] {
            case SetKeys(keys) =>
              println(s"init: set: $keys")
              st.unstashAll(beh(keys))
            case x =>
              println(s"init: stash: $x")
              st.stash(x)
              Behaviors.same
          }
        }
      )

    def beh(
        keys: KeySet = KeySet(Nil)
    ): Behavior[PublicKeyMessage] = {
      Behaviors.setup(ctx =>
        Behaviors.withTimers[PublicKeyMessage] { timer =>
          timer.startTimerWithFixedDelay(FetchKeys, keyRefreshInterval)
          Behaviors.receiveMessage {
            case GetKeys(replyTo) =>
              println(s"beh: get $keys")
              replyTo ! keys
              Behaviors.same
            case SetKeys(keys) =>
              println(s"beh: set $keys")
              beh(keys)
            case FetchKeys =>
              println(s"beh: fetch new")
              ctx.pipeToSelf(oidcClient.fetchKeys) {
                case Success(value) =>
                  SetKeys(value)
                case Failure(exception) =>
                  println("WARNING: could not refresh keys")
                  exception.printStackTrace()
                  SetKeys(keys)
              }
              Behaviors.same
          }
        }
      )
    }

    implicit val timeout: Timeout = Timeout(5.seconds)
    implicit val sch: typed.Scheduler = actorSystem.scheduler

    import actorSystem.executionContext

    private val actorRefF: Future[ActorRef[PublicKeyMessage]] =
      actorSystem ? (Spawn[PublicKeyMessage](
        PublicKeyManagerActor.init,
        "PublicKeyManagerActor",
        Props.empty,
        _
      ))

    def getKeys: Future[KeySet] =
      for {
        ar <- actorRefF
        keys <- ar ? GetKeys
      } yield keys
  }
}
