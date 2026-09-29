import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.5.2"
ThisBuild / scalacOptions += "-Wconf:msg=Flag.*repeatedly:s"

lazy val microservice = Project("supplementary-information-api", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
  )
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
  )
  .settings(CodeCoverageSettings.settings)
  .settings(PlayKeys.playDefaultPort := 7759)
  .settings(routesGenerator := InjectedRoutesGenerator)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(
    Test / fork := true,
    Test / javaOptions += "-Dlogger.resource=logback-test.xml")
  .settings(libraryDependencies ++= AppDependencies.itDependencies)
