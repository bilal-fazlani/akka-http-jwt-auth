inThisBuild(
  Seq(
    scalaVersion := "3.0.0-M3",
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
    testFrameworks += TestFramework("munit.Framework"),
    parallelExecution in Test in ThisBuild := false
  )
)

lazy val `akka-http-jwt-auth-root` = project
  .in(file("."))
  .settings(
    skip in publish := true,
    name := "akka-http-jwt-auth-root"
  )
  .aggregate(
    `akka-http-jwt-auth`,
    `akka-http-oidc-client`,
    `akka-http-client-circe`,
    `test-utils`,
    example
  )

lazy val `akka-http-oidc-client` = project
  .in(file("./akka-http-oidc-client"))
  .settings(
    name := "akka-http-oidc-client",
    libraryDependencies ++= Seq(
      Libs.`akka-actor-typed`,
      TestLibs.`embedded-keycloak` % Test,
      Libs.`slf4j-simple` % Test
    ).map(_.withDottyCompat(scalaVersion.value)) ++ Seq(
      TestLibs.munit % Test
    )
  )
  .dependsOn(`akka-http-client-circe`, `test-utils` % Test)

lazy val `akka-http-jwt-auth` = project
  .in(file("./akka-http-jwt-auth"))
  .settings(
    name := "akka-http-jwt-auth",
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`jwt-core`,
      TestLibs.logback,
      TestLibs.`embedded-keycloak` % Test,
      Libs.`slf4j-simple` % Test
    ).map(_.withDottyCompat(scalaVersion.value)) ++ Seq(
      TestLibs.munit % Test
    )
  )
  .dependsOn(`akka-http-oidc-client`, `test-utils` % Test)

lazy val `akka-http-client-circe` = project
  .in(file("./akka-http-client-circe"))
  .settings(
    name := "akka-http-client-circe",
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-stream`,
      Json.`akka-http-circe`,
      TestLibs.logback % Test,
      Libs.`slf4j-simple` % Test,
      Libs.`akka-actor-typed` % Test
    ).map(_.withDottyCompat(scalaVersion.value)) ++ Seq(
      TestLibs.munit % Test
    )
  ).dependsOn(`test-utils` % Test)

lazy val `test-utils` = project
  .in(file("./test-utils"))
  .settings(
    name := "test-utils",
    skip in publish := true,
    libraryDependencies ++= Seq(
      Json.`akka-http-circe`,
      Libs.`akka-http`,
      Libs.`akka-actor-typed`,
      TestLibs.`embedded-keycloak`
    ).map(_.withDottyCompat(scalaVersion.value))++ Seq(
      TestLibs.munit
    )
  )

lazy val example = project
  .in(file("./example"))
  .settings(
    name := "example",
    skip in publish := true,
    libraryDependencies ++= Seq(
      Libs.`akka-http`,
      Libs.`akka-actor-typed`,
      Libs.`slf4j-simple`,
      TestLibs.`embedded-keycloak`
    ).map(_.withDottyCompat(scalaVersion.value))
  )
  .dependsOn(`akka-http-jwt-auth`)
