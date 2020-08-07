import sbt._

object Libs {
  lazy val `akka-http` = "com.typesafe.akka" %% "akka-http" % "10.2.0"
  lazy val `akka-actor-typed` =
    "com.typesafe.akka" %% "akka-actor-typed" % "2.6.8"
  lazy val `akka-stream` = "com.typesafe.akka" %% "akka-stream" % "2.6.8"
  lazy val `borer-core` = "io.bullet" %% "borer-core" % "1.6.1"
  lazy val `borer-derivation` = "io.bullet" %% "borer-derivation" % "1.6.1"
}
