package io.datumo.logger

import org.apache.spark.sql.SparkSession
import org.scalatest.funsuite.AnyFunSuite

class SparkFunSuite extends AnyFunSuite {
  implicit val spark: SparkSession =
    SparkSession
      .builder()
      .master("local")
      .appName("Unit tests")
      .config("spark.sql.shuffle.partitions", "1")
      .config("spark.ui.enabled", "false")
      .config("spark.ui.showConsoleProgress", "false")
      .getOrCreate()
}
