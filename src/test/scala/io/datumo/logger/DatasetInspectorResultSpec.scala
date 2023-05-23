package io.datumo.logger

import io.datumo.logger.DatasetInspectorResult.{getDatasetRecords, getRowFields}
import io.datumo.logger.DatasetLoggerSuite.customerToRecordFields
import org.apache.spark.sql.Row
import org.scalatest.matchers.should.Matchers


class DatasetInspectorResultSpec extends SparkFunSuite with Matchers {
  test("Should construct DatasetInspectorResultSpec from computed Datasets") {
    import spark.implicits._

    val inspectedDatasetRecords = Seq(CustomerTestSchema("id", "value"))
    val extraQueryDatasetRecords = Seq(CustomerTestSchema("extra", "query", 123))

    val inspectedDataset = inspectedDatasetRecords.toDS
    val extraQueryDataset = extraQueryDatasetRecords.toDS
    val maxRowsNum = 100

    val inspectedDatasetRecordsFields = inspectedDatasetRecords.map(customerToRecordFields)
    val extraQueryDatasetRecordsFields = extraQueryDatasetRecords.map(customerToRecordFields)
    val expectedDatasetInspectorResult = DatasetInspectorResult(
      inspectedDatasetRecordsFields,
      inspectedDatasetRecords.length,
      Some(extraQueryDatasetRecordsFields)
    )

    DatasetInspectorResult(inspectedDataset, Some(extraQueryDataset), maxRowsNum) shouldBe expectedDatasetInspectorResult
  }

  test("Should convert a Row to record's fields") {
    val row = Row("id0", 12.34, 100, null)
    val cols = Seq("id", "price", "quantity", "comment")

    val expectedRecordFields = Map(
      "id" -> "id0",
      "price" -> 12.34,
      "quantity" -> 100,
      "comment" -> null
    )
    getRowFields(row, cols) shouldBe expectedRecordFields
  }

  test("Should convert a Dataset to records' fields considering maximal records num") {
    import spark.implicits._
    val records = Seq(
      CustomerTestSchema("id0", "e0"),
      CustomerTestSchema("id1", "e1", 1),
      CustomerTestSchema("id2", "e2")
    )
    val dataset = records.toDS

    val maxRowsNum = 2
    val expectedRecordsFields = records.map(customerToRecordFields).slice(0, maxRowsNum)

    getDatasetRecords(dataset, maxRowsNum) should contain theSameElementsAs expectedRecordsFields
  }
}
