// Modeled after https://github.com/rtimush/sbt-updates/blob/master/proguard.sbt

proguardSettings

ProguardKeys.proguardVersion in Proguard := "4.11"

ProguardKeys.options in Proguard ++= Seq(
  "-dontoptimize",
  "-dontusemixedcaseclassnames", // Don't write out i.class and I.class (which won't unjar properly on case-insensitive file systems like on OSX)
  "-keep class fm.** { *; }",
  "-repackageclasses 'fm.common.libs'",
  "-keepattributes",
  "-keepparameternames"
)

ProguardKeys.defaultInputFilter in Proguard := Some("!META-INF/**,!org/apache/commons/codec/language/bm/*.txt")

// Some of the Apache libs need javax.crypto
ProguardKeys.libraries in Proguard += new File(System.getProperty("java.home"), "lib/jce.jar")

ProguardKeys.inputs in Proguard <<= (dependencyClasspath in Embedded, packageBin in Runtime) map {
  (dcp, pb) => Seq(pb) ++ dcp.files
}

Build.publishMinJar <<= (ProguardKeys.proguard in Proguard) map (_.head)

packagedArtifact in (Compile, packageBin) <<= (packagedArtifact in (Compile, packageBin), Build.publishMinJar) map {
  case ((art, _), jar) => (art, jar)
}

dependencyClasspath in Compile <++= dependencyClasspath in Embedded

dependencyClasspath in Test <++= dependencyClasspath in Embedded