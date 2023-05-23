name := "CustomerReport"
version := "0.1.0"
scalaVersion := "2.13.10"

val sparkVersion = "3.3.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
  "ch.qos.logback" % "logback-classic" % "1.3.5",
  "io.datumo" % "dataset-logger" % "0.1.0"
)
