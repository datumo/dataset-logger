package io.datumo.logger

import io.datumo.logger.DatasetTransforms.flattenSchema
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, Row}

private[logger] trait DatasetLogFormatter {

  def formatLogMessage(message: String, datasetInspectorResult: DatasetInspectorResult, messageId: String): String = {
    val header = formatHeader(datasetInspectorResult.datasetSize, messageId)
    val formattedDatasetContent = formatDataset(datasetInspectorResult.datasetRecords)
    val extraQueryDataset = datasetInspectorResult.extraQueryDatasetRecords match {
      case Some(datasetRecords) => Seq(extraQueryDatasetHeader, formatDataset(datasetRecords))
      case None                 => Seq.empty
    }
    (Seq(message, header, formattedDatasetContent) ++ extraQueryDataset).mkString(
      s"\n$LogMessagePrefix\t",
      s"\n$LogMessagePrefix\t----------------\n",
      LogMessagePrefix
    )
  }

  private[logger] val extraQueryDatasetHeader = s"$LogMessagePrefix\t----- EXTRA SQL QUERY RESULT -----"

  private[logger] def formatHeader[A](datasetCount: Int, messageId: String): String =
    s"$LogMessagePrefix[$messageId] Dataset contains $datasetCount matching rows:"

  private[logger] def formatRowContent(rowContent: Map[String, Any]): String =
    rowContent.mkString(s"$LogMessagePrefix\t| ", " | ", " |")

  private[logger] def formatDataset[A](datasetRecords: Seq[RecordFields]): String =
    datasetRecords.map(formatRowContent).mkString("\n", "\n", "\n")

}
