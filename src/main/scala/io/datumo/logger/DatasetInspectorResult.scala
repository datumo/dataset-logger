package io.datumo.logger

import io.datumo.logger.DatasetTransforms.flattenSchema
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, Row}

case class DatasetInspectorResult(
  datasetRecords: Seq[RecordFields],
  datasetSize: Int,
  extraQueryDatasetRecords: Option[Seq[RecordFields]] = None
)

object DatasetInspectorResult {
  def apply[A, B](
    inspectedDataset: Dataset[A],
    extraQueryDataset: Option[Dataset[B]],
    maxRowsNum: Int
  ): DatasetInspectorResult = {
    val datasetContent = getDatasetRecords(inspectedDataset, maxRowsNum)
    val datasetSize = inspectedDataset.count().toInt
    val extraQueryDatasetContent = extraQueryDataset.map(getDatasetRecords(_, Int.MaxValue))
    DatasetInspectorResult(datasetContent, datasetSize, extraQueryDatasetContent)
  }

  private[logger] def getRowFields(row: Row, columns: Seq[String]): RecordFields =
    row.toSeq.zip(columns).map { case (colValue, colName) => colName -> colValue }.toMap

  private[logger] def getDatasetRecords[A](dataset: Dataset[A], maxRowsNum: Int): Seq[RecordFields] = {
    val schema = flattenSchema(dataset.schema)
    val loggedDataFrame = dataset.select(schema.map(col): _*).take(maxRowsNum)
    loggedDataFrame.map(row => getRowFields(row, schema)).toSeq
  }
}
