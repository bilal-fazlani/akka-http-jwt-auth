import sbt._

object Json {
//  lazy val `borer-core` = "io.bullet" %% "borer-core" % "1.6.3"
//  lazy val `borer-akka` = "io.bullet" %% "borer-compat-akka" % "1.6.3"
  // lazy val `akka-http-jackson` =
    // "de.heikoseeberger" %% "akka-http-jackson" % "1.35.2"
  lazy val `akka-http-circe` = "de.heikoseeberger" %% "akka-http-circe" % "1.35.3"
}

object Libs {
  private val AkkaVersion = "2.6.10"
  private val AkkaHttpVersion = "10.2.3"
  
  lazy val `slf4j-simple` = "org.slf4j" % "slf4j-simple" % "2.0.0-alpha1"
  lazy val `akka-http` = "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion
  lazy val `akka-actor-typed` =
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion
  lazy val `akka-stream` = "com.typesafe.akka" %% "akka-stream" % AkkaVersion
  lazy val `jwt-core` = "com.pauldijou" %% "jwt-core" % "5.0.0"
}

object TestLibs {
  lazy val `embedded-keycloak` =
    "com.github.tmtsoftware.embedded-keycloak" %% "embedded-keycloak" % "7fd5623"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.21"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
