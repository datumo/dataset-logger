package io.datumo.logger

import io.datumo.logger.DatasetTransforms.flattenSchema
import org.apache.spark.sql.functions.{col, lit}
import org.apache.spark.sql.{Column, DataFrame, Dataset, SparkSession}

object DatasetInspector {
  def inspect[A](
    dataset: Dataset[A],
    messageId: String,
    maxRowsNum: Int = MaxRowsNum
  )(implicit spark: SparkSession, datasetLoggerConfig: DatasetLoggerConfig): DatasetInspectorResult = {
    val columnRenameMapping = getColumnRenameMapping(messageId)
    val filteredDataset = getLoggedDatasetContent(dataset, columnRenameMapping)
    val extraQueryDataset = runExtraQueryIfDefined(dataset, messageId)
    DatasetInspectorResult(filteredDataset, extraQueryDataset, maxRowsNum)
  }

  private[logger] def getColumnRenameMapping(
    messageId: String
  )(implicit datasetLoggerConfig: DatasetLoggerConfig): Map[String, String] =
    datasetLoggerConfig.columnRenameMapping
      .flatMap { renameMapping =>
        renameMapping
          .get(messageId)
          .map(_.map { case (key, value) => key.toLowerCase -> value.toLowerCase })
      }
      .getOrElse(Map.empty)

  private[logger] def getLoggedDatasetContent[A](
    dataset: Dataset[A],
    columnRenameMapping: Map[String, String]
  )(implicit datasetLoggerConfig: DatasetLoggerConfig): Dataset[A] =
    dataset.filter(buildMessageFilter(dataset, columnRenameMapping))

  private[logger] def runExtraQueryIfDefined[A](
    dataset: Dataset[A],
    messageId: String
  )(implicit spark: SparkSession, datasetLoggerConfig: DatasetLoggerConfig): Option[DataFrame] = {
    val extraQueries = datasetLoggerConfig.extraQueries.getOrElse(Map.empty)
    extraQueries.get(messageId).map { query =>
      dataset.createOrReplaceTempView(ExtraQueryDatasetName)
      spark.sql(query)
    }
  }

  private[logger] def buildMessageFilter[A](
    dataset: Dataset[A],
    columnRenameMapping: Map[String, String]
  )(implicit datasetLoggerConfig: DatasetLoggerConfig): Column = {
    val columnsToFilter = getFilterForLoggedColumns(columnRenameMapping)
    val datasetColumns = flattenSchema(dataset.schema).map(_.toLowerCase)
    val existingColumnsFilters =
      for {
        (columnName, filter) <- columnsToFilter
        if datasetColumns.contains(columnName) || columnRenameMapping.values.exists(_ == columnName)
      } yield filter
    existingColumnsFilters.reduceOption(_ or _).getOrElse(lit(false))
  }

  private[logger] def getFilterForLoggedColumns(
    columnRenameMapping: Map[String, String]
  )(implicit datasetLoggerConfig: DatasetLoggerConfig): Seq[(String, Column)] = {
    val trackedColumnValues = datasetLoggerConfig.loggedColumns
    trackedColumnValues.map { columnValue =>
      val trackedColumnName = columnValue.columnName.toLowerCase
      val columnName = columnRenameMapping
        .getOrElse(trackedColumnName, trackedColumnName)
      (columnName, buildFilter(columnName, columnValue.columnValues))
    }
  }

  private[logger] def buildFilter(columnName: String, columnValues: Seq[String]): Column =
    col(columnName).isin(columnValues: _*)
}
