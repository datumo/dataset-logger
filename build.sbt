name := "dataset-logger"
version := "0.1.0"

val Scala213 = "2.13.10"
val Scala212 = "2.12.15"

scalaVersion := Scala213
crossScalaVersions := List(Scala212, Scala213)

val circeVersion = "0.14.4"
val sparkVersion = "3.3.2"

crossPaths := false

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test"
)

ThisBuild / organization := "io.datumo"
ThisBuild / organizationName := "datumo"
ThisBuild / organizationHomepage := Some(url("https://datumo.io/"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/datumo/dataset-logger"),
    "scm:git@github.com:datumo/dataset-logger.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id = "wiatrak2",
    name = "Wojciech Pratkowiecki",
    email = "wojciech.pratkowiecki@datumo.pl",
    url = url("https://datumo.io")
  )
)

ThisBuild / description := "Apache Spark Scala utility to track data records during application execution"
ThisBuild / licenses := List(
  "Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")
)
ThisBuild / homepage := Some(url("https://github.com/datumo/dataset-logger"))

ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true
