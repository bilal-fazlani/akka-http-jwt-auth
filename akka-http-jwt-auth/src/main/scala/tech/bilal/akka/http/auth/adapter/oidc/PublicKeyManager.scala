package tech.bilal.akka.http.auth.adapter.oidc

import akka.actor.typed
import akka.actor.typed.SpawnProtocol.{Command, Spawn}
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, ActorSystem, Behavior, Props}
import akka.util.Timeout

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

class PublicKeyManager(
    oidcClient: OIDCClient,
    keyRefreshInterval: FiniteDuration
)(implicit
    actorSystem: ActorSystem[Command]
) {

  private sealed trait PublicKeyMessage
  private case class GetKey(kid: String, replyTo: ActorRef[Option[Key]])
      extends PublicKeyMessage
  private case class SetKeys(keys: KeySet) extends PublicKeyMessage
  private case object FetchKeys extends PublicKeyMessage

  private case class State(map: Map[String, Key])
  private object State {
    def apply(keys: KeySet): State =
      new State(keys.keys.map(k => (k.kid, k)).toMap)
  }

  private implicit val timeout: Timeout = Timeout(5.seconds)
  private implicit val sch: typed.Scheduler = actorSystem.scheduler

  import actorSystem.executionContext

  private val actorRefF: Future[ActorRef[PublicKeyMessage]] =
    actorSystem ? (Spawn[PublicKeyMessage](
      PublicKeyManagerActor.init,
      "PublicKeyManagerActor",
      Props.empty,
      _
    ))

  def getKey(kid: String): Future[Option[Key]] =
    for {
      ar <- actorRefF
      key <- ar ? (GetKey(kid, _))
    } yield key

  private object PublicKeyManagerActor {
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
              st.unstashAll {
                beh(State(keys))
              }
            case x =>
              println(s"init: stash: $x")
              st.stash(x)
              Behaviors.same
          }
        }
      )

    def beh(state: State): Behavior[PublicKeyMessage] = {
      Behaviors.setup(ctx =>
        Behaviors.withTimers[PublicKeyMessage] { timer =>
          timer.startTimerWithFixedDelay(FetchKeys, keyRefreshInterval)
          Behaviors.receiveMessage {
            case GetKey(kid, replyTo) =>
              println(s"beh: get kid $kid from $state")
              replyTo ! state.map.get(kid)
              Behaviors.same
            case SetKeys(keys) =>
              println(s"beh: set keys ${state.map}")
              beh(State(keys))
            case FetchKeys =>
              println(s"beh: fetch new")
              import ctx.executionContext
              oidcClient.fetchKeys
                .map(keys => ctx.self ! SetKeys(keys))
                .recover {
                  case NonFatal(_) =>
                    println("WARNING: could not refresh keys")
                }
              Behaviors.same
          }
        }
      )
    }
  }
}
