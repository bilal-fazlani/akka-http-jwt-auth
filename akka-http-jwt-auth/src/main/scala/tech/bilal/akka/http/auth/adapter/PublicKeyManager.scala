package tech.bilal.akka.http.auth.adapter

import akka.actor.typed.SpawnProtocol.{Command, Spawn}
import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.Behaviors
import akka.pattern.AskSupport
import akka.util.Timeout
import tech.bilal.akka.http.oidc.client.OIDCClient
import tech.bilal.akka.http.oidc.client.models.{Key, KeySet}

import scala.concurrent.Future
import scala.concurrent.duration.{DurationInt, FiniteDuration}
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

  given timeout:Timeout = Timeout(5.seconds)
  given akka.actor.typed.Scheduler = actorSystem.scheduler

  import actorSystem.executionContext

  private val actorRefF: Future[ActorRef[PublicKeyMessage]] =
    actorSystem ? (Spawn[PublicKeyMessage](
      PublicKeyManagerActor.initializing,
      "PublicKeyManagerActor",
      Props.empty,
      _
    ))

  def getKey(kid: String): Future[Either[KeyError,Key]] =
    for {
      ar <- actorRefF
      key <- ar.ask[Either[KeyError,Key]](x => GetKey(kid, x))
    } yield key

  private object PublicKeyManagerActor {
    def initializing: Behavior[PublicKeyMessage] =
      Behaviors.setup[PublicKeyMessage] { ctx =>
        ctx.pipeToSelf(oidcClient.fetchKeys) {
          case Success(value) => SetKeys(value)
          case Failure(exception) => Disconnect
        }
        Behaviors.withStash[PublicKeyMessage](1024) { st =>
          Behaviors.receiveMessagePartial[PublicKeyMessage] {
            case SetKeys(keys) =>
              println(s"init: set: $keys")
              st.unstashAll(operational(State(keys)))
            case Disconnect =>
              println("init: disconnect")
              st.unstashAll(disconnected)
            case g @ GetKey(_, _) =>
              println("init: get")
              st.stash(g)
              Behaviors.same
          }
        }
      }

    def disconnected: Behavior[PublicKeyMessage] = Behaviors.withTimers { timer =>
      timer.startTimerWithFixedDelay(RefreshKeys, authConfig.keyRefreshIntervalWhenDisconnected)
      Behaviors.setup{ctx =>
        Behaviors.receiveMessagePartial[PublicKeyMessage] {
          case GetKey(_, replyTo) =>
            println("disconnected: get")
            replyTo ! Left(KeyError.AuthServerDisconnected)
            Behaviors.same
          case RefreshKeys =>
            println("disconnected: refresh")
            oidcClient.fetchKeys.onComplete {
              case Success(keySet) =>
                ctx.self ! SetKeys(keySet)
              case Failure(exception) =>
                println(s"disconnected: key refresh failed. will retry after ${authConfig.keyRefreshIntervalWhenDisconnected}")
            }
            Behaviors.same
          case SetKeys(keys) =>
            println(s"disconnected: set: $keys")
            operational(State(keys))
        }
      }
    }
    
    def operational(state: State): Behavior[PublicKeyMessage] = {
      Behaviors.setup(ctx =>
        Behaviors.withTimers[PublicKeyMessage] { timer =>
          timer.startTimerWithFixedDelay(RefreshKeys, authConfig.keyRefreshInterval)
          Behaviors.receiveMessage {
            case GetKey(kid, replyTo) =>
              println(s"operational: get kid")
              state.map.get(kid) match {
                case Some(key) => 
                  replyTo ! Right(key)
                case None =>
                  replyTo ! Left(KeyError.KeyNotFound)
              }
              Behaviors.same
            case RefreshKeys =>
              println(s"operational: refresh")
              oidcClient.fetchKeys.onComplete{ result =>
                result match {
                  case Success(keys) => 
                    ctx.self ! SetKeys(keys)
                  case Failure(exception) =>
                    println(s"operational: key refresh failed. will retry after ${authConfig.keyRefreshInterval}")
                }
              }
              Behaviors.same
            case SetKeys(keys) =>
              println(s"operational: set: $keys")
              operational(State(keys))
          }
        }
      )
    }
  }
}
