FMPublic

name := "fm-common"

version := "0.4.0-SNAPSHOT"

description := "Common Scala classes that we use at Frugal Mechanic that have no required external dependencies."

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings")

javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.1.3" % "provided",
  "com.google.guava" % "guava" % "19.0" % "embedded",
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3" % "embedded",
  "com.sun.mail" % "javax.mail" % "1.5.2" % "provided",
  "commons-codec" % "commons-codec" % "1.10" % "embedded",
  "commons-io" % "commons-io" % "2.4" % "embedded",
  "it.unimi.dsi" % "fastutil" % "7.0.12" % "embedded",
  "org.apache.commons" % "commons-compress" % "1.10" % "embedded",
  "org.apache.commons" % "commons-lang3" % "3.4" % "embedded",
  //"org.atteo" % "evo-inflector" % "1.2" % "embedded",
  "org.bouncycastle" % "bcprov-jdk15on" % "1.54" % "embedded",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.1" % "embedded",
  "org.slf4j" % "slf4j-api" % "1.7.13" % "provided",
  "org.tukaani" % "xz" % "1.5" % "embedded",  // Used by commons-compress and should be synced up with whatever version commons-compress requires
  "org.xerial.snappy" % "snappy-java" % "1.1.1" % "provided"  // SnappyOutputStream might be messed up in 1.1.1.3
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "provided,test"
