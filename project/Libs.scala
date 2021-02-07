import sbt._

object Json {
  private val circeVersion = "0.14.0-M3"
  
  lazy val `circe-core` = "io.circe" %% "circe-core" % circeVersion
  lazy val `circe-generic` = "io.circe" %% "circe-generic" % circeVersion
  lazy val `circe-parser` = "io.circe" %% "circe-parser" % circeVersion
}

object Libs {
  private val AkkaVersion = "2.6.10"
  private val AkkaHttpVersion = "10.2.3"
  
  lazy val `slf4j-simple` = "org.slf4j" % "slf4j-simple" % "2.0.0-alpha1"
  lazy val `akka-http` = ("com.typesafe.akka" %% "akka-http" % AkkaHttpVersion).cross(CrossVersion.For3Use2_13())
  lazy val `akka-actor-typed` = ("com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion).cross(CrossVersion.For3Use2_13())
  lazy val `akka-testkit` = ("com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion).cross(CrossVersion.For3Use2_13())
  lazy val `akka-stream` = ("com.typesafe.akka" %% "akka-stream" % AkkaVersion).cross(CrossVersion.For3Use2_13())
  lazy val `jwt-core` = ("com.pauldijou" %% "jwt-core" % "5.0.0").cross(CrossVersion.For3Use2_13())
}

object TestLibs {
  lazy val `embedded-keycloak` =
    ("com.github.tmtsoftware.embedded-keycloak" %% "embedded-keycloak" % "7fd5623").cross(CrossVersion.For3Use2_13())
  lazy val munit = "org.scalameta" %% "munit" % "0.7.21"
  lazy val logback = "ch.qos.logback" % "logback-classic" % "1.2.3"
}
