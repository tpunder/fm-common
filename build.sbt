//
// Note: fm-common is setup to cross build with Scala.js
//

scalaVersion in ThisBuild := "2.12.2"

crossScalaVersions in ThisBuild := Seq("2.11.11", "2.12.2")

lazy val `fm-common` = project.in(file(".")).
  aggregate(fmCommonJS, fmCommonJVM).
  settings(
    publish := {},
    publishLocal := {},
    publishArtifact := false,
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))) // http://stackoverflow.com/a/18522706
  )
  
lazy val `fm-common-macros` = project.in(file("macro")).settings(
  publish := {},
  publishLocal := {},
  publishArtifact := false,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))), // http://stackoverflow.com/a/18522706
  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value
)

lazy val `fm-common-` = crossProject.in(file(".")).
  settings((FMPublic ++ Seq(
    name := "fm-common",
    version := "0.9.0-SNAPSHOT",
    description := "Common Scala classes that we use at Frugal Mechanic / Eluvio",
    scalacOptions := Seq(
      "-unchecked",
      "-deprecation",
      "-language:implicitConversions",
      "-feature",
      "-Xlint",
      "-Ywarn-unused-import"
    ) ++ (if (scalaVersion.value.startsWith("2.12")) Seq(
      // Scala 2.12 specific compiler flags
      "-opt:l:project"
    ) else Nil),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    EclipseKeys.useProjectId := true,
    // include the macro classes and resources in the main jar
    mappings in (Compile, packageBin) ++= { mappings in (`fm-common-macros`, Compile, packageBin) }.value,
    // include the macro sources in the main source jar
    mappings in (Compile, packageSrc) ++= { mappings in (`fm-common-macros`, Compile, packageSrc) }.value,
    
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.1" % "provided,test"
  )):_*).
  jvmSettings(Seq(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.google.guava" % "guava" % "21.0",
      "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
      "com.sun.mail" % "javax.mail" % "1.5.2" % "provided",
      "com.fasterxml.woodstox" % "woodstox-core" % "5.0.2",
      "commons-codec" % "commons-codec" % "1.10",
      "commons-io" % "commons-io" % "2.5",
      "it.unimi.dsi" % "fastutil" % "7.0.13",
      "org.apache.commons" % "commons-compress" % "1.13",
      "org.apache.commons" % "commons-lang3" % "3.5",
      "org.bouncycastle" % "bcprov-jdk15on" % "1.56",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.tukaani" % "xz" % "1.6",  // Used by commons-compress and should be synced up with whatever version commons-compress requires
      "org.xerial.snappy" % "snappy-java" % "1.1.2.6"
    )
  ):_*).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2",
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.1"
  )

lazy val fmCommonJVM = `fm-common-`.jvm.dependsOn(`fm-common-macros` % "compile-internal, test-internal")
lazy val fmCommonJS = `fm-common-`.js.dependsOn(`fm-common-macros` % "compile-internal, test-internal")

