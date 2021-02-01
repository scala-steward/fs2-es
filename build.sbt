import xerial.sbt.Sonatype._
//Deps
val agitationV = "0.2.0"
val catsEffectV = "2.3.1"
val fs2V = "2.5.0"
val munitV = "0.7.21"

val scala213 = "2.13.4"
val scala212 = "2.12.13"

val commonSettings = Seq(
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint"),
  scalacOptions := Seq("-target:jvm-1.8")
)

lazy val root = (project in file("."))
  .aggregate(core.js, core.jvm, testing.js, testing.jvm)
  .settings(
    commonSettings,
    publish / skip := true
  )

lazy val core = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("core"))
  .settings(
    commonSettings,
    name := "fs2-es",
    libraryDependencies ++= Seq(
      "co.fs2" %%% "fs2-core" % fs2V,
      "io.chrisdavenport" %%% "agitation" % agitationV,
      "org.typelevel" %%% "cats-effect-laws" % catsEffectV % Test,
      "org.scalameta" %%% "munit-scalacheck" % munitV % Test
    ),
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := Seq(scala212, scala213)
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )

lazy val testing = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("testing"))
  .settings(
    commonSettings,
    name := "fs2-es-testing",
    testFrameworks += new TestFramework("munit.Framework"),
    crossScalaVersions := Seq(scala212, scala213)
  )
  .jsSettings(
    scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
  )
  .dependsOn(core % "compile->compile;test->test")

lazy val docs = (project in file("fs2-es-docs"))
  .settings(
    scalacOptions ~= filterConsoleScalacOptions,
    publish / skip := true
  )
  .dependsOn(testing.jvm)
  .enablePlugins(MdocPlugin)

ThisBuild / scalaVersion := scala213

ThisBuild / developers := List(
  Developer(
    "sloshy",
    "Ryan Peters",
    "me@rpeters.dev",
    url("https://blog.rpeters.dev/")
  )
)
ThisBuild / homepage := Some(url("https://github.com/sloshy/fs2-es/"))
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / organization := "dev.rpeters"

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))
ThisBuild / githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release")))
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
