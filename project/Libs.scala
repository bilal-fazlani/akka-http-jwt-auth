import sbt._

object Json {
  private val circeVersion = "0.14.1"

  lazy val `circe-core` = "io.circe" %% "circe-core" % circeVersion
  lazy val `circe-generic` = "io.circe" %% "circe-generic" % circeVersion
  lazy val `circe-parser` = "io.circe" %% "circe-parser" % circeVersion
}

object Libs {
  private val AkkaVersion = "2.6.14"
  private val AkkaHttpVersion = "10.2.4"

  lazy val `slf4j-simple` = "org.slf4j" % "slf4j-simple" % "2.0.0-alpha1"
  lazy val `akka-http` = ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion)
    .cross(CrossVersion.for3Use2_13)
  lazy val `akka-actor-typed` =
    ("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion).cross(
      CrossVersion.for3Use2_13
    )
  lazy val `akka-testkit` =
    ("com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion).cross(
      CrossVersion.for3Use2_13
    )
  lazy val `akka-stream` = ("com.typesafe.akka" %% "akka-stream" % AkkaVersion)
    .cross(CrossVersion.for3Use2_13)
  lazy val `jwt-core` = "com.github.jwt-scala" %% "jwt-core" % "8.0.1"
}

object TestLibs {
  lazy val `embedded-keycloak` =
    ("com.github.tmtsoftware.embedded-keycloak" %% "embedded-keycloak" % "7fd5623")
      .cross(CrossVersion.for3Use2_13)
  lazy val munit = "org.scalameta" %% "munit" % "0.7.26"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
