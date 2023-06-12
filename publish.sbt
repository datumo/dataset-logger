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
    id = "michalmisiewicz",
    name = "Michał Misiewicz",
    email = "michal.misiewicz@datumo.pl",
    url = url("https://github.com/michalmisiewicz")
  ),
  Developer(
    id = "wiatrak2",
    name = "Wojciech Pratkowiecki",
    email = "wojciech.pratkowiecki@datumo.pl",
    url = url("https://github.com/wiatrak2")
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

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
