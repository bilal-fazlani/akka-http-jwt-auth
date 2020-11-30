package tech.bilal.akka.http.auth.adapter

case class AuthConfig(
    issuer: String,
    supportedAlgorithms: Set[String]
)
