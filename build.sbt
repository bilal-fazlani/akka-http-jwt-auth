inThisBuild(
  Seq(
    scalaVersion := "2.13.3",
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
    testFrameworks += new TestFramework("munit.Framework")
  )
)

lazy val `akka-http-jwt-auth-root` = project
  .in(file("."))
  .settings(
    name := "akka-http-jwt-auth-root"
  )
  .aggregate(`akka-http-jwt-auth`, example)

lazy val `akka-http-jwt-auth-models` = project
  .in(file("./akka-http-jwt-auth-models"))
  .settings(
    name := "akka-http-jwt-auth-models",
    libraryDependencies ++= Seq(
      Libs.`borer-core`,
      Libs.`borer-derivation`
    )
  )

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
  .dependsOn(`akka-http-jwt-auth-models`)

lazy val example = project
  .in(file("./example"))
  .settings(
    name := "example",
    skip in publish := true,
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-actor-typed`,
      TestLibs.`embedded-keycloak`
    )
  )
  .dependsOn(`akka-http-jwt-auth`)
