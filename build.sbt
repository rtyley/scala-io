import sbtrelease.ReleaseStateTransformations._

organization in ThisBuild := "com.madgag"

licenses in ThisBuild := Seq("Scala" -> url("http://www.scala-lang.org/license/"))

lazy val commonSettings = Seq(
  scalaVersion := "2.12.3",
  apiMappings += {
    /*
 * The rt.jar file is located in the path stored in the sun.boot.class.path system property.
 * See the Oracle documentation at http://docs.oracle.com/javase/6/docs/technotes/tools/findingclasses.html.
 */
    val rtJar: String = System.getProperty("sun.boot.class.path").split(java.io.File.pathSeparator).collectFirst {
      case str: String if str.endsWith(java.io.File.separator + "rt.jar") => str
    }.get // fail hard if not found

    sbt.file(rtJar) -> url("http://docs.oracle.com/javase/8/docs/api/index.html")
  },
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
)


lazy val coreProj = Project("scala-io-core",file("core")).enablePlugins(TutPlugin).settings(
  commonSettings,
  libraryDependencies += "com.jsuereth" %% "scala-arm" % "2.0"
)

lazy val fileProj = Project("scala-io-file",file("file"))
  .settings(commonSettings,
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
  )
  .dependsOn(coreProj % "test->test;compile->compile")

releaseCrossBuild in ThisBuild := true // true if you cross-build the project for multiple Scala versions

releasePublishArtifactsAction in ThisBuild := PgpKeys.publishSigned.value // Use publishSigned in publishArtifacts step

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  publishArtifacts,
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeReleaseAll"),
  pushChanges
)
