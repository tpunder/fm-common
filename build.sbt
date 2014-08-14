FMPublic

name := "fm-common"

version := "0.2.0-SNAPSHOT"

description := "Common Scala classes that we use at Frugal Mechanic that have no required external dependencies."

scalaVersion := "2.11.2"

// Note: Use "++ 2.11.0" to select a specific version when building
crossScalaVersions := Seq("2.10.4", "2.11.2")

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings")

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "provided",
  "org.xerial.snappy" % "snappy-java" % "1.1.1",  // SnappyOutputStream might be messed up in 1.1.1.3
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3" % "embedded",
  "org.apache.commons" % "commons-compress" % "1.8.1" % "embedded",
  "org.apache.commons" % "commons-lang3" % "3.3.2" % "embedded",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.51" % "embedded",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.0" % "embedded",
  "org.tukaani" % "xz" % "1.5" % "embedded",  // Used by commons-compress and should be synced up with whatever version commons-compress requires
  "commons-codec" % "commons-codec" % "1.9" % "embedded",
  "commons-io" % "commons-io" % "2.4" % "embedded"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "provided,test"
