import sbt.util

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.1"

ThisBuild / resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

ThisBuild / evictionErrorLevel := util.Level.Info

lazy val root = (project in file("."))
  .settings(
    name := "comparative-http"
  )

lazy val shared = Project("shared", file("shared"))

lazy val zioHttp = Project("zio-http", file("zio-http"))
  .settings(
    libraryDependencies += "dev.zio" %% "zio"      % "2.0.18",
    libraryDependencies += "dev.zio" %% "zio-http" % "3.0.0-RC2+115-3a6525ce-SNAPSHOT",
  )
  .dependsOn(shared)

lazy val http4s = Project("http4s", file("http4s"))
  .settings(
    libraryDependencies += "org.http4s" %% "http4s-ember-client" % "1.0.0-M40",
    libraryDependencies += "org.http4s" %% "http4s-ember-server" % "1.0.0-M40",
    libraryDependencies += "org.http4s" %% "http4s-dsl"          % "1.0.0-M40",
    libraryDependencies += "org.http4s" %% "http4s-circe"        % "1.0.0-M40",
    libraryDependencies += "io.circe"   %% "circe-generic"       % "0.14.5",
  )
  .dependsOn(shared)

lazy val akkaHttp = Project("akka-http", file("akka-http"))
  .settings(
    libraryDependencies += "com.typesafe.akka" %% "akka-http"        % "10.5.3",
    libraryDependencies += "com.typesafe.akka" %% "akka-actor-typed" % "2.8.0",
    libraryDependencies += "com.typesafe.akka" %% "akka-stream"      % "2.8.0",
    libraryDependencies += "com.typesafe.play" %% "play-json"        % "2.10.1",
  )
  .dependsOn(shared)

lazy val play = Project("play", file("play"))
  .settings(libraryDependencies += guice)
  .enablePlugins(PlayScala)
  .disablePlugins(PlayLayoutPlugin)
  .dependsOn(shared)
