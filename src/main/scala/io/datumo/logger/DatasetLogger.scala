package io.datumo.logger

import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.storage.StorageLevel

class DatasetLogger(configJson: String) extends DatasetLogFormatter {
  private[logger] lazy val datasetLoggerConfig: Option[DatasetLoggerConfig] = DatasetLoggerConfig(configJson)

  def this(configJson: Option[String]) = this(configJson.orEmpty)

  def logDataset[A](
    message: String,
    dataset: Dataset[A],
    messageId: String,
    maxRowsNum: Int = MaxRowsNum,
    cacheLevel: Option[StorageLevel] = Some(StorageLevel.MEMORY_AND_DISK)
  )(implicit spark: SparkSession): String =
    (datasetLoggerConfig, shouldLogMessageBeSkipped(messageId)) match {
      case (Some(loggerConfig), false) =>
        implicit val implicitDatasetLoggerConfig = loggerConfig
        val datasetInspectorResult =
          DatasetInspector.inspect(cacheDataset(dataset, cacheLevel), messageId, maxRowsNum)
        formatLogMessage(message, datasetInspectorResult, messageId)
      case _ => message
    }

  private[logger] def shouldLogMessageBeSkipped(messageId: String) = {
    val logOnly = datasetLoggerConfig.flatMap(_.logOnly)
    logOnly.isDefined && !logOnly.exists(_.contains(messageId))
  }

  private[logger] def cacheDataset[A](
    dataset: Dataset[A],
    cacheLevel: Option[StorageLevel]
  ): Dataset[A] =
    cacheLevel match {
      case Some(level) => dataset.persist(level)
      case _           => dataset
    }
}
