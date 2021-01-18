import sbt._

object Libs {
  lazy val `akka-http` = "com.typesafe.akka" %% "akka-http" % "10.2.2"
  lazy val `akka-actor-typed` =
    "com.typesafe.akka" %% "akka-actor-typed" % "2.6.11"
  lazy val `akka-stream` = "com.typesafe.akka" %% "akka-stream" % "2.6.11"
  lazy val `borer-core` = "io.bullet" %% "borer-core" % "1.6.3"
  lazy val `borer-derivation` = "io.bullet" %% "borer-derivation" % "1.6.3"
  lazy val `borer-akka` = "io.bullet" %% "borer-compat-akka" % "1.6.3"
  lazy val `jwt-core` = "com.pauldijou" %% "jwt-core" % "4.2.0"
}

object TestLibs {
  lazy val `embedded-keycloak` =
    "com.github.tmtsoftware.embedded-keycloak" %% "embedded-keycloak" % "7fd5623"
  lazy val munit = "org.scalameta" %% "munit" % "0.7.20"
}
