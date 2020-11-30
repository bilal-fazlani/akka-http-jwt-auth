inThisBuild(
  Seq(
    scalaVersion := "3.0.0-M2",
    resolvers += "jitpack" at "https://jitpack.io",
    organization := "tech.bilal",
    homepage := Some(
      url("https://github.com/bilal-fazlani/akka-http-jwt-auth")
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/bilal-fazlani/akka-http-jwt-auth"),
        "git@github.com:bilal-fazlani/akka-http-jwt-auth.git"
      )
    ),
    developers := List(
      Developer(
        "bilal-fazlani",
        "Bilal Fazlani",
        "bilal.m.fazlani@gmail.com",
        url("https://bilal-fazlani.com")
      )
    ),
    licenses += ("MIT", url(
      "https://github.com/bilal-fazlani/akka-http-jwt-auth/blob/master/LICENSE"
    )),
    crossPaths := true,
    Test / parallelExecution := false,
    testFrameworks += TestFramework("munit.Framework")
  )
)

lazy val `akka-http-jwt-auth-root` = project
  .in(file("."))
  .settings(
    name := "akka-http-jwt-auth-root"
  )
  .aggregate(`akka-http-jwt-auth`, `akka-http-oidc-client`, example)

lazy val `akka-http-oidc-client` = project
  .in(file("./akka-http-oidc-client"))
  .settings(
    name := "akka-http-oidc-client",
    libraryDependencies ++= Seq(
      Libs.`akka-actor-typed`,
      Libs.`akka-http`,
      Libs.`akka-stream`,
      Libs.`borer-core`,
      Libs.`borer-akka`
    ).map(_.withDottyCompat(scalaVersion.value))
  )

lazy val `akka-http-jwt-auth` = project
  .in(file("./akka-http-jwt-auth"))
  .settings(
    name := "akka-http-jwt-auth",
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`jwt-core`,
      Libs.`borer-core`,
      TestLibs.`embedded-keycloak` % Test
    ).map(_.withDottyCompat(scalaVersion.value)) ++ Seq(
      TestLibs.munit % Test
    )
  )
  .dependsOn(`akka-http-oidc-client`)

lazy val example = project
  .in(file("./example"))
  .settings(
    name := "example",
    skip in publish := true,
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-actor-typed`,
      TestLibs.`embedded-keycloak`
    ).map(_.withDottyCompat(scalaVersion.value))
  )
  .dependsOn(`akka-http-jwt-auth`)
