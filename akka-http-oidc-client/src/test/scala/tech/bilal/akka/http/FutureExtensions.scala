package tech.bilal.akka.http

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.concurrent.{Await, ExecutionContext, Future}

extension [T] (future: Future[T]) 
  def block(duration:FiniteDuration): T = Await.result(future, duration)
  def block: T = Await.result(future, 5.seconds)
  def tap(thunk: T => Unit)(using ec: ExecutionContext): Future[T] = future.map[T]{ x =>
    thunk(x)
    x
  }
    
