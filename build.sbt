//
// Note: fm-common is setup to cross build with Scala.js
//

scalaVersion in ThisBuild := "2.12.0"

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
  libraryDependencies <+= scalaVersion("org.scala-lang" % "scala-compiler" % _)
)

lazy val `fm-common-` = crossProject.in(file(".")).
  settings((FMPublic ++ Seq(
    name := "fm-common",
    version := "0.7.0-SNAPSHOT",
    description := "Common Scala classes that we use at Frugal Mechanic that have no required external dependencies.",
    scalacOptions := Seq("-unchecked", "-deprecation", "-language:implicitConversions", "-feature", "-Xlint", "-Ywarn-unused-import", "-opt:l:classpath"),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    EclipseKeys.useProjectId := true,
    // include the macro classes and resources in the main jar
    mappings in (Compile, packageBin) <++= mappings in (`fm-common-macros`, Compile, packageBin),
    // include the macro sources in the main source jar
    mappings in (Compile, packageSrc) <++= mappings in (`fm-common-macros`, Compile, packageSrc),
    
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.0.0" % "provided,test"
  )):_*).
  jvmSettings(Seq(
    // Add JVM-specific settings here
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.1.3" % "provided",
      "com.google.guava" % "guava" % "19.0",
      "com.googlecode.juniversalchardet" % "juniversalchardet" % "1.0.3",
      "com.sun.mail" % "javax.mail" % "1.5.2" % "provided",
      "commons-codec" % "commons-codec" % "1.10",
      "commons-io" % "commons-io" % "2.5",
      "it.unimi.dsi" % "fastutil" % "7.0.12",
      "org.apache.commons" % "commons-compress" % "1.12",
      "org.apache.commons" % "commons-lang3" % "3.4",
      "org.bouncycastle" % "bcprov-jdk15on" % "1.54",
      "org.codehaus.woodstox" % "woodstox-core-asl" % "4.4.1",
      "org.slf4j" % "slf4j-api" % "1.7.13" % "provided",
      "org.tukaani" % "xz" % "1.5",  // Used by commons-compress and should be synced up with whatever version commons-compress requires
      "org.xerial.snappy" % "snappy-java" % "1.1.1" % "provided"  // SnappyOutputStream might be messed up in 1.1.1.3
    )
  ):_*).
  jsSettings(
    // Add JS-specific settings here
    libraryDependencies += "be.doeraene" %%% "scalajs-jquery" % "0.9.1",
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.1",
    libraryDependencies += "org.scala-js" %%% "scalajs-java-time" % "0.2.0"
  )

lazy val fmCommonJVM = `fm-common-`.jvm.dependsOn(`fm-common-macros` % "compile-internal, test-internal")
lazy val fmCommonJS = `fm-common-`.js.dependsOn(`fm-common-macros` % "compile-internal, test-internal")

