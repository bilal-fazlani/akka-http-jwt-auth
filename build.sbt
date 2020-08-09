inThisBuild(
  Seq(
    version := "0.1.0",
    scalaVersion := "2.13.3",
    resolvers += "jitpack" at "https://jitpack.io",
    testFrameworks += new TestFramework("munit.Framework")
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "akka-http-auth"
  )
  .aggregate(adapter, borer, keycloak, example)

lazy val adapter = project
  .in(file("./adapter"))
  .settings(
    name := "akka-http-auth-adapter",
    libraryDependencies ++= Seq(
      Libs.`akka-actor-typed`,
      Libs.`akka-stream`,
      Libs.`akka-http`,
      Libs.`jwt-core`,
      Libs.`borer-core`,
      Libs.`borer-derivation`,
      Libs.`borer-akka`,
      TestLibs.munit % Test,
      TestLibs.`embedded-keycloak` % Test
    )
  )

lazy val borer = project
  .in(file("./borer"))
  .settings(
    name := "akka-http-auth-adapter-borer",
    libraryDependencies ++= Seq(
      Libs.`borer-core`,
      Libs.`borer-derivation`
    )
  )
  .dependsOn(adapter)

lazy val keycloak = project
  .in(file("./keycloak"))
  .settings(
    name := "akka-http-auth-adapter-keycloak",
    libraryDependencies ++= Seq(
    )
  )

lazy val example = project
  .in(file("./example"))
  .settings(
    name := "akka-http-auth-example",
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-actor-typed`
    )
  )
  .dependsOn(adapter, borer, keycloak)
