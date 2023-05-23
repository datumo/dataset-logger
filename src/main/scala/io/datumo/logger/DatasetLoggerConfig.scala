package io.datumo.logger

import com.typesafe.scalalogging.Logger
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._

final case class LoggedColumnConfig(columnName: String, columnValues: Seq[String])

final case class DatasetLoggerConfig(
  loggedColumns: Seq[LoggedColumnConfig],
  extraQueries: Option[ExtraQueries] = None,
  logOnly: Option[Seq[String]] = None,
  columnRenameMapping: Option[ColumnRenameMapping] = None
)

object DatasetLoggerConfig {
  private val logger = Logger(getClass.getName)

  def apply(configJson: String): Option[DatasetLoggerConfig] =
    if (configJson.nonEmpty) {
      logger.info("Initializing DatasetLoggerConfig with provided JSON configuration.")
      Option(parseConfigJson(configJson))
    } else {
      logger.warn("No configuration for DatasetLoggerConfig provided. Will not be able to inspect DataFrames content")
      None
    }

  def apply(configJson: Option[String]): Option[DatasetLoggerConfig] =
    DatasetLoggerConfig(configJson.orEmpty)

  def parseConfigJson(configJson: String): DatasetLoggerConfig = {
    val columnFiltersConfigDecodeParam = Decoder[Seq[LoggedColumnConfig]].prepare(_.downField(ColumnFiltersConfigKey))
    val extraQueriesDecodeParam = Decoder[Option[ExtraQueries]].prepare(_.downField(ExtraQueriesConfigKey))
    val logOnlyDecodeParam = Decoder[Option[Seq[String]]].prepare(_.downField(LogOnlyConfigKey))
    val columnRenameMappingDecodeParam =
      Decoder[Option[ColumnRenameMapping]].prepare(_.downField(ColumnRenameMappingConfigKey))

    val loggerConfig = for {
      columnValues <- decode(configJson)(columnFiltersConfigDecodeParam)
      columnValuesParsed = columnValues.map(columnConfig =>
        columnConfig.copy(columnName = columnConfig.columnName.toLowerCase)
      )
      extraQueries <- decode(configJson)(extraQueriesDecodeParam)
      logOnly <- decode(configJson)(logOnlyDecodeParam)
      columnRenameMapping <- decode(configJson)(columnRenameMappingDecodeParam)
    } yield DatasetLoggerConfig(columnValuesParsed, extraQueries, logOnly, columnRenameMapping)

    loggerConfig match {
      case Right(parsedConfig) => parsedConfig
      case Left(e) =>
        println(e); throw InvalidDatasetLoggerConfigException(s"Could not parse logger config - error: $e", e)
    }
  }
}
