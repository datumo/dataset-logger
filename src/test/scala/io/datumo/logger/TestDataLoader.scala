package io.datumo.logger

import io.circe.parser._

trait TestDataLoader {
  def parseJson(path: String): String =
    parse(readJsonFromFile(path))
      .fold(e => throw new RuntimeException(s"Could not load test resource from $path", e), identity)
      .toString

    private def readJsonFromFile(path: String): String = {
      val jsonSource = scala.io.Source.fromResource(path)
      try jsonSource.mkString
      finally jsonSource.close
    }
}
