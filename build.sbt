//
// Note: fm-common is setup to cross build with Scala.js
//

scalaVersion in ThisBuild := "2.11.8"

lazy val root = project.in(file(".")).
  aggregate(fmCommonJS, fmCommonJVM).
  settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))) // http://stackoverflow.com/a/18522706
  )

lazy val `fm-common-` = crossProject.in(file(".")).
  settings((FMPublic ++ Seq(
    name := "fm-common",
    version := "0.6.0-SNAPSHOT",
    description := "Common Scala classes that we use at Frugal Mechanic that have no required external dependencies.",
    scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-optimise", "-Yinline-warnings"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    EclipseKeys.useProjectId := true,

    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "provided,test"
  
  )):_*).
  jvmSettings((FMProguardSettings ++ Seq(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.3" % "provided",
      "com.google.guava" % "guava" % "19.0" % "embedded",
      "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3" % "embedded",
      "com.sun.mail" % "javax.mail" % "1.5.2" % "provided",
      "commons-codec" % "commons-codec" % "1.10" % "embedded",
      "commons-io" % "commons-io" % "2.5" % "embedded",
      "it.unimi.dsi" % "fastutil" % "7.0.12" % "embedded",
      "org.apache.commons" % "commons-compress" % "1.12" % "embedded",
      "org.apache.commons" % "commons-lang3" % "3.4" % "embedded",
      "org.bouncycastle" % "bcprov-jdk15on" % "1.54" % "embedded",
      "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.1" % "embedded",
      "org.slf4j" % "slf4j-api" % "1.7.13" % "provided",
      "org.tukaani" % "xz" % "1.5" % "embedded",  // Used by commons-compress and should be synced up with whatever version commons-compress requires
      "org.xerial.snappy" % "snappy-java" % "1.1.1" % "provided"  // SnappyOutputStream might be messed up in 1.1.1.3
    ),
    
    ProguardKeys.options in Proguard ++= Seq(
      "-dontoptimize",
      "-dontusemixedcaseclassnames", // Don't write out i.class and I.class (which won't unjar properly on case-insensitive file systems like on OSX)
      "-keep class fm.** { *; }",
      """-keepclassmembers enum * {
          public static **[] values();
          public static ** valueOf(java.lang.String);
      }""",
      "-repackageclasses 'fm.common.libs'",
      "-keepattributes",
      "-keepparameternames",
      "-dontnote org.apache.commons.lang3.ObjectUtils",
      "-dontnote org.apache.commons.io.LineIterator",
      "-dontnote org.bouncycastle.jcajce.provider.**",
      "-dontwarn com.ctc.wstx.**",
      "-dontwarn com.google.**"
    ),

    ProguardKeys.defaultInputFilter in Proguard := Some("!META-INF/**,!org/apache/commons/codec/language/bm/*.txt,!javax/**"),

    // Some of the Apache libs need javax.crypto
    ProguardKeys.libraries in Proguard += new File(System.getProperty("java.home"), "lib/jce.jar")
  )):_*).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.0",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.0",
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0"
  )

lazy val fmCommonJVM = `fm-common-`.jvm
lazy val fmCommonJS = `fm-common-`.js

