import sbt._
import backln.Docker._

name := "images.social"

organization := "backln"

version in ThisBuild := "1-SNAPSHOT"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "jsonz" %% "jsonz" % "1.0.0",

  "org.joda" % "joda-convert" % "1.7",
  "joda-time" % "joda-time" % "2.7",
  "commons-io" % "commons-io" % "2.4",
  "commons-lang" % "commons-lang" % "2.6",

  "ch.qos.logback" % "logback-core" % "1.0.13",
  "ch.qos.logback" % "logback-classic" % "1.0.13",

  "org.slf4j" % "slf4j-api" % "1.7.10",
  "org.slf4j" % "log4j-over-slf4j" % "1.7.10",

  "org.scalaz" %% "scalaz-core" % "7.1.0",

  "com.amazonaws" % "aws-java-sdk-s3" % "1.9.22",

  "com.typesafe.akka" %% "akka-http-experimental" % "1.0-M4",
  "com.typesafe.akka" %% "akka-http-core-experimental" % "1.0-M4",
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-M4"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http-testkit-experimental" % "1.0-M4" % "test"
)

resolvers ++= Seq(
  "bintray-banno-oss-releases" at "http://dl.bintray.com/banno/oss",
  "Sonatype Staging Repo" at "https://oss.sonatype.org/content/repositories/staging"
)

scalacOptions ++= Seq(
  "-deprecation", "-feature", "-language:postfixOps", "-Xlint", "-Xlog-free-terms", "-Xlog-free-types",
  "-language:implicitConversions", "-language:higherKinds", "-language:existentials", "-language:postfixOps",
  "-Ywarn-dead-code", "-Ywarn-numeric-widen", "-Ywarn-inaccessible", "-unchecked"
)

scalacOptions in Test ++= Seq(
  "-language:reflectiveCalls"
)

backln.Docker.settings ++ Seq(
  baseImage in docker := "java:7"
)

buildInfoSettings ++ Seq(
  buildInfoPackage := "images",
  sourceGenerators in Compile <+= buildInfo,
  buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)
)

shellPrompt <<= name { name => state => s"${name} > " }
