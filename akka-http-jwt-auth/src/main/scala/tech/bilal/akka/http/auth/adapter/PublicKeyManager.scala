package tech.bilal.akka.http.auth.adapter

import akka.actor.typed.SpawnProtocol.{Command, Spawn}
import akka.actor.typed.*
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.AskSupport
import akka.util
import akka.util.Timeout
import tech.bilal.akka.http.oidc.client.OIDCClient
import tech.bilal.akka.http.oidc.client.models.{Key, KeySet, OIDCConfig}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import scala.annotation.tailrec
import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

enum KeyError:
  case KeyNotFound, AuthServerDisconnected

class PublicKeyManager(
    oidcClient: OIDCClient,
    authConfig: AuthConfig
)(using actorSystem: ActorSystem[Command]) {

  private sealed trait PublicKeyMessage
  private case class GetKey(kid: String, replyTo: ActorRef[Either[KeyError, Key]])
      extends PublicKeyMessage
  private case class SetKeys(keys: KeySet) extends PublicKeyMessage
  private case object Disconnect extends PublicKeyMessage
  private case object RefreshKeys extends PublicKeyMessage

  private case class State(map: Map[String, Key])
  private object State {
    def apply(keys: KeySet): State =
       State(keys.keys.map(k => (k.kid, k)).toMap)
  }

  given akka.actor.typed.Scheduler = actorSystem.scheduler

  import actorSystem.executionContext

  private val keyManagerActorRef: Future[ActorRef[PublicKeyMessage]] =
    given Timeout = Timeout(2.seconds)
    actorSystem ? (Spawn[PublicKeyMessage](
      PublicKeyManagerActor.initializing,
      "PublicKeyManagerActor",
      Props.empty,
      _
    ))

  def getKey(kid: String): Future[Either[KeyError,Key]] =
    for {
      ar <- keyManagerActorRef
      given Timeout = Timeout(authConfig.keyFetchTimeout)
      key <- ar.ask[Either[KeyError,Key]](x => GetKey(kid, x))
    } yield key

  private object PublicKeyManagerActor {
    def initializing: Behavior[PublicKeyMessage] = { 
      Behaviors.setup[PublicKeyMessage] { ctx =>
        ctx.pipeToSelf(oidcClient.fetchKeys(authConfig.keyFetchTimeout)) {
          case Success(value) => 
            SetKeys(value)
          case Failure(exception) =>
            println(exception)  
            Disconnect
        }
        Behaviors.withStash[PublicKeyMessage](1024) { st =>
          Behaviors.receiveMessagePartial[PublicKeyMessage] {
            case SetKeys(keys) =>
              st.unstashAll(operational(State(keys)))
            case Disconnect =>
              st.unstashAll(disconnected)
            case g@GetKey(_, _) =>
              if(st.isFull) st.clear()
              st.stash(g)
              Behaviors.same
          }
        }
      }
    }

    def disconnected: Behavior[PublicKeyMessage] =
      Behaviors.withTimers { timer =>
          timer.startTimerWithFixedDelay(RefreshKeys, authConfig.keyRefreshIntervalWhenDisconnected)
          Behaviors.setup { ctx =>
            Behaviors.receiveMessagePartial[PublicKeyMessage] {
              case Disconnect =>
                Behaviors.same
              case GetKey(_, replyTo) =>
                replyTo ! Left(KeyError.AuthServerDisconnected)
                Behaviors.same
              case RefreshKeys =>
                ctx.pipeToSelf(oidcClient.fetchKeys(authConfig.keyFetchTimeout)) {
                  case Success(value) => 
                    SetKeys(value)
                  case Failure(exception) =>
                    println(exception)
                    Disconnect
                }
                Behaviors.same
              case SetKeys(keys) =>
                operational(State(keys))
            }
          }
        }

    def operational(state: State): Behavior[PublicKeyMessage] =
      Behaviors.setup { ctx =>
        Behaviors.withTimers[PublicKeyMessage] { timer =>
          timer.startTimerWithFixedDelay(RefreshKeys, authConfig.keyRefreshInterval)
          Behaviors.receiveMessage {
            case Disconnect => 
              //ignore
              Behaviors.same
            case GetKey(kid, replyTo) =>
              state.map.get(kid) match {
                case Some(key) =>
                  replyTo ! Right(key)
                case None =>
                  replyTo ! Left(KeyError.KeyNotFound)
              }
              Behaviors.same
            case RefreshKeys =>
              oidcClient.fetchKeys(authConfig.keyFetchTimeout).onComplete { result =>
                result match {
                  case Success(keys) =>
                    ctx.self ! SetKeys(keys)
                  case Failure(exception) =>
                    println(exception)
                }
              }
              Behaviors.same
            case SetKeys(keys) =>
              operational(State(keys))
          }
        }
      }
  }
}
