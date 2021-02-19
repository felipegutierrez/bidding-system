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

  // Scala test
  "org.scalatest" %% "scalatest" % scalaTestVersion,

  // Log
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
)
