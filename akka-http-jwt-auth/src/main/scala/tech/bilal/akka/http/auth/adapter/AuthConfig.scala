package tech.bilal.akka.http.auth.adapter

import scala.concurrent.duration.{DurationInt, FiniteDuration}

case class AuthConfig(
    realm: String,
    openIdConfigUrl: String,
    issuerCheck: Boolean = true,
    supportedAlgorithms: Set[String] = Set("RS256"),
    keyFetchTimeout: FiniteDuration = 10.seconds,
    keyRefreshInterval: FiniteDuration = 1.days,
    keyRefreshIntervalWhenDisconnected: FiniteDuration = 5.minutes
)
