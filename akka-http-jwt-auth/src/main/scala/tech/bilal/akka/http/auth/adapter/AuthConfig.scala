package tech.bilal.akka.http.auth.adapter

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class AuthConfig(
    issuerCheck: Boolean = true,
    supportedAlgorithms: Set[String] = Set("RS256"),
    keyRefreshInterval:FiniteDuration = 1.days,
    keyRefreshIntervalWhenDisconnected:FiniteDuration = 5.minutes
)
