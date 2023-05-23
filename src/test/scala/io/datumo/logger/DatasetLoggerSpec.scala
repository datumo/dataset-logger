package io.datumo.logger

import org.apache.spark.storage.StorageLevel
import org.scalatest.matchers.should.Matchers

class DatasetLoggerSpec extends SparkFunSuite with Matchers {
  import DatasetLoggerSuite._

  test("Should determine whether to skip a message") {
    val newDatasetLogger = new DatasetLogger("") {
      override lazy val datasetLoggerConfig = Some(DatasetLoggerConfig(Seq.empty, logOnly = Some(Seq("log2", "log4"))))
    }

    datasetLogger.shouldLogMessageBeSkipped("log0") shouldBe false
    newDatasetLogger.shouldLogMessageBeSkipped("log0") shouldBe true
    newDatasetLogger.shouldLogMessageBeSkipped("log2") shouldBe false
  }

  test("Should skip unwanted message") {
    import spark.implicits._

    val dataset = Seq(CustomerTestSchema()).toDS
    val newDatasetLogger = new DatasetLogger("") {
      override lazy val datasetLoggerConfig = Some(
        DatasetLoggerConfig(loggedColumnValuesConfig, logOnly = Some(Seq("log2", "log4")))
      )
    }

    newDatasetLogger.logDataset("test message", dataset, "log0") shouldBe "test message"
  }

  test("Should cache dataset if needed") {
    import spark.implicits._

    val dataset = Seq(CustomerTestSchema()).toDS

    datasetLogger.cacheDataset(dataset, None).storageLevel.useMemory shouldBe false
    datasetLogger.cacheDataset(dataset, Some(StorageLevel.MEMORY_AND_DISK)).storageLevel.useMemory shouldBe true
  }
}
