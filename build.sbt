name := "fm-common"

organization := "com.frugalmechanic"

version := "0.1-SNAPSHOT"

description := "Common Scala classes that we use at Frugal Mechanic that have no required external dependencies."

licenses := Seq("Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/frugalmechanic/fm-common"))

scalaVersion := "2.10.4"

// Note: Use "++ 2.11.0" to select a specific version when building
crossScalaVersions := Seq("2.10.4", "2.11.0")

scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-optimise")

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.5" % "provided",
  "ch.qos.logback" % "logback-classic" % "1.0.13" % "provided",
  "org.xerial.snappy" % "snappy-java" % "1.1.0.1" % "provided",
  "org.codehaus.woodstox" % "woodstox-core-asl" % "4.3.0" % "provided",
  "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3" % "embedded",
  "org.apache.commons" % "commons-compress" % "1.8" % "embedded",
  "org.apache.commons" % "commons-lang3" % "3.2.1" % "embedded",
  "commons-codec" % "commons-codec" % "1.9" % "embedded",
  "commons-io" % "commons-io" % "2.4" % "embedded"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.3" % "provided,test"

publishMavenStyle := true

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT")) 
    Some("snapshots" at nexus + "content/repositories/snapshots") 
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <developers>
    <developer>
      <id>tim</id>
      <name>Tim Underwood</name>
      <email>tim@frugalmechanic.com</email>
      <organization>Frugal Mechanic</organization>
      <organizationUrl>http://frugalmechanic.com</organizationUrl>
    </developer>
  </developers>
  <scm>
      <connection>scm:git:git@github.com:frugalmechanic/fm-common.git</connection>
      <developerConnection>scm:git:git@github.com:frugalmechanic/fm-common.git</developerConnection>
      <url>git@github.com:frugalmechanic/fm-common.git</url>
  </scm>)

