package io.datumo.logger

import org.scalatest.matchers.should.Matchers
import org.scalatest.funsuite.AnyFunSuite


class DatasetLoggerConfigSpec extends AnyFunSuite with Matchers with TestDataLoader {
  lazy val loggerConfig = DatasetLoggerConfig(parseJson("logger_config.json"))

  test("Should load logger config without error") {
    noException shouldBe thrownBy(loggerConfig)
  }

  test("Should initialize empty logger when no configuration is provided") {
    val emptyDatasetLoggerConfig: Option[DatasetLoggerConfig] = None
    DatasetLoggerConfig("") shouldBe emptyDatasetLoggerConfig
  }

  test("Should load logger config without optional fields") {
    val configString = parseJson("logger_config_no_optional.json")
    noException shouldBe thrownBy(DatasetLoggerConfig.parseConfigJson(configString))
  }

  test("Should parse the config correctly") {
    val loggedColumns = loggerConfig.map(_.loggedColumns).getOrElse(Seq.empty)
    val extraQueries = loggerConfig.flatMap(_.extraQueries).getOrElse(Map.empty)
    val logOnly = loggerConfig.flatMap(_.logOnly).getOrElse(Seq.empty)
    val columnRenameMapping = loggerConfig.flatMap(_.columnRenameMapping).getOrElse(Map.empty)
    loggedColumns should contain theSameElementsAs Seq(LoggedColumnConfig("customerId".toLowerCase, Seq("123", "456")))
    extraQueries should contain theSameElementsAs Map("log1" -> "SELECT * FROM dataset WHERE country IN ['PL', 'US']")
    logOnly should contain theSameElementsAs Seq("log1", "log2", "log3")
    columnRenameMapping should contain theSameElementsAs Map("log3" -> Map("customerId" -> "clientId"))
  }

  test("Should fail when tracked column values are provided incorrectly") {
    val invalidConfigString = parseJson("logger_config_invalid_column_filters.json")
    an[InvalidDatasetLoggerConfigException] should be thrownBy DatasetLoggerConfig.parseConfigJson(invalidConfigString)
  }

  test("Should fail when extra filters are provided incorrectly") {
    val invalidConfigString = parseJson("logger_config_invalid_extra_filters.json")
    an[InvalidDatasetLoggerConfigException] should be thrownBy DatasetLoggerConfig.parseConfigJson(invalidConfigString)
  }
}
