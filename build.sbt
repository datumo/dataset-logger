name := "dataset-logger"
version := "0.1.0"

val Scala213 = "2.13.10"
val Scala212 = "2.12.15"

scalaVersion := Scala213
crossScalaVersions := List(Scala212, Scala213)

val circeVersion = "0.14.4"
val sparkVersion = "3.3.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion % Provided,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test"
)
