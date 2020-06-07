ThisBuild / scalaVersion := "2.13.2"
ThisBuild / organization := "geronimus"
ThisBuild / name := "mise-en-place"
ThisBuild / version := "0.1.0-SNAPSHOT"

val scalatest = "org.scalatest" %% "scalatest" % "3.1.2"

lazy val mesonplass = ( project in file( "." ) )
  .settings(
    libraryDependencies += scalatest,
    assemblyJarName in assembly := s"${ ( ThisBuild / name ).value }-${ version.value }.jar"
  )

