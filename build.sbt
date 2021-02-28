name := "scala-vista-data"
organization := "com.github.jordenk"
version := "0.1-SNAPSHOT"
scalaVersion := "2.13.3"

resolvers += "Sonatype Public".at("https://oss.sonatype.org/content/groups/public/")

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "co.fs2" %% "fs2-core" % "2.4.2",
  "co.fs2" %% "fs2-io" % "2.4.2",
  "io.chrisdavenport" %% "log4cats-slf4j" % "1.0.1",
  "io.circe" %% "circe-parser" % circeVersion,
  "org.typelevel" %% "cats-core" % "2.3.0",
  "org.typelevel" %% "cats-effect" % "2.3.0",
  "org.scalactic" %% "scalactic" % "3.2.2",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.slf4j" % "slf4j-simple" % "1.7.26"
)

fork in run := true
outputStrategy := Some(StdoutOutput)
connectInput in run := true

scalafmtOnCompile := true

scalacOptions ++= List(
  "-feature",
  "-language:higherKinds",
  "-Xlint",
  "-Yrangepos",
  "-Ywarn-unused"
)
scalafixDependencies in ThisBuild += "com.nequissimus" %% "sort-imports" % "0.1.3"

enablePlugins(UniversalPlugin, JavaAppPackaging)
