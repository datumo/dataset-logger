package io.datumo

package object logger {
  type ExtraQueries = Map[String, String]
  type ColumnRenameMapping = Map[String, Map[String, String]]
  type RecordFields = Map[String, Any]

  final val ColumnFiltersConfigKey = "columnFiltersConfig"
  final val ExtraQueriesConfigKey = "extraQuery"
  final val LogOnlyConfigKey = "logOnly"
  final val ColumnRenameMappingConfigKey = "columnRenameMapping"

  final val ExtraQueryDatasetName = "dataset"

  final val LogMessagePrefix = "[DatasetLogger]"
  final val MaxRowsNum = 100

  implicit class OptionString(str: Option[String]) {
    def orEmpty: String = str.getOrElse("")
  }

  final case class InvalidDatasetLoggerConfigException(message: String, e: Throwable)
      extends RuntimeException(message, e: Throwable)
}
