
lazy val commonSettings = Seq(
  organization := "com.example",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.3",
  libraryDependencies += "com.novocode" % "junit-interface" % "0.11" % "test"
)


lazy val coreProj = (project in file("core")).enablePlugins(TutPlugin).settings(
  commonSettings,
  libraryDependencies += "com.jsuereth" %% "scala-arm" % "2.0"
)

lazy val fileProj = (project in file("file"))
  .settings(commonSettings,
    libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.5"
  )
  .dependsOn(coreProj % "test->test;compile->compile")
