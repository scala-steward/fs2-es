//Deps
val fs2V = "2.1.0"

lazy val root = (project in file("."))
  .settings(
    name := "fs2-es",
    organization := "dev.rpeters",
    libraryDependencies ++= Seq(
      "co.fs2" %% "fs2-io" % fs2V,
      "io.chrisdavenport" %% "agitation" % "0.2.0-M1",
      "org.typelevel" %% "cats-effect-laws" % "2.0.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.2" % Test,
      "org.scalatest" %% "scalatest" % "3.0.8" % Test
    ),
    publishTo := sonatypePublishToBundle.value,
    crossScalaVersions := Seq("2.12.10", "2.13.1")
  )

lazy val docs = (project in file("fs2-es-docs"))
  .settings(
    scalacOptions ~= filterConsoleScalacOptions,
    scalaVersion := "2.13.0", //Fix for a weird bug going on
    publish / skip := true
  )
  .dependsOn(root)
  .enablePlugins(MdocPlugin)

ThisBuild / scalaVersion := "2.13.1"

publishMavenStyle := true

licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

import xerial.sbt.Sonatype._
sonatypeProjectHosting := Some(GitHubHosting("sloshy", "fs2-es", "me@rpeters.dev"))

import ReleaseTransformations._

releaseCrossBuild := true
releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  runTest,
  setReleaseVersion,
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)
