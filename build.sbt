name := "bidding-system"

version := "0.1"

// scalaVersion := "2.13.4"
scalaVersion := "2.12.7"

val scalaBinVersion = "2.12"
val scalaTestVersion = "3.2.0"
val akkaVersion = "2.6.12"
val akkaHttpVersion = "10.2.2"
val logbackVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val jwtSprayVersion = "4.3.0"
val junitVersion = "4.13"
val scalaTestPlusVersion = "3.2.3.0"
val mockitoScalaVersion = "1.16.5"

resolvers += Resolver.jcenterRepo

// ####### Dockerfile settings #######
enablePlugins(JavaAppPackaging, JavaServerAppPackaging, DockerPlugin, AshScriptPlugin)
import NativePackagerHelper._

packageName in Docker := packageName.value
version in Docker := version.value
dockerExposedPorts := List(8080, 8081, 8082, 8083)
dockerLabels := Map("felipeogutierrez" -> "felipe.o.gutierrez@gmail.com")
dockerBaseImage := "openjdk:jre-alpine"
dockerRepository := Some("felipeogutierrez")
defaultLinuxInstallLocation in Docker := "/usr/local"
daemonUser in Docker := "daemon"
mappings in Universal ++= directory( baseDirectory.value / "src" / "main" / "resources" )
bashScriptExtraDefines += """addApp "--bidders http://host.docker.internal:8081,http://host.docker.internal:8082,http://host.docker.internal:8083""""
// ####### Dockerfile settings #######

libraryDependencies ++= Seq(
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion,

  // akka streams
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,

  // Akka HTTP: overwrites are required because Akka-gRPC depends on 10.1.x
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http2-support" % akkaHttpVersion,

  // JWT
  "com.pauldijou" %% "jwt-spray-json" % jwtSprayVersion,

  // Scala test + junit
  "org.scalatest" %% "scalatest" % scalaTestVersion,
  "junit" % "junit" % junitVersion % Test,
  "org.scalatestplus" %% "junit-4-13" % scalaTestPlusVersion % "test",

  // Log + Mockito
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion,
)
