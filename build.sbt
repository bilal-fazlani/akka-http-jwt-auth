inThisBuild(
  Seq(
    version := "0.1.0",
    scalaVersion := "2.13.3",
    resolvers += "jitpack" at "https://jitpack.io",
    Test / parallelExecution := false,
    testFrameworks += new TestFramework("munit.Framework")
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "root"
  )
  .aggregate(`akka-http-jwt-auth`, example)

lazy val `akka-http-jwt-auth` = project
  .in(file("./akka-http-jwt-auth"))
  .settings(
    name := "akka-http-jwt-auth",
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

lazy val example = project
  .in(file("./example"))
  .settings(
    name := "example",
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-actor-typed`,
      TestLibs.`embedded-keycloak`
    )
  )
  .dependsOn(`akka-http-jwt-auth`)
