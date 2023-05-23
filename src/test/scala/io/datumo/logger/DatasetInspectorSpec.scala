package io.datumo.logger

import org.apache.spark.sql.functions.{col, lit}
import org.scalatest.matchers.should.Matchers

class DatasetInspectorSpec extends SparkFunSuite with Matchers {
  import DatasetLoggerSuite._

  test("Should inspect dataset") {
    import spark.implicits._
    val matchingRows = Seq(
      CustomerTestSchema("id0", "e0"),
      CustomerTestSchema("id1", "e100")
    )
    val notMatchingRows = Seq(
      CustomerTestSchema("id2", "e3", 2),
      CustomerTestSchema("id3", "e3")
    )
    val dataset = (matchingRows ++ notMatchingRows).toDS

    val expectedDatasetRecords = matchingRows.map(customerToRecordFields)
    val expectedDatasetSize = 2
    val expectedExtraQueryDatasetRecords: Option[Seq[RecordFields]] = None
    val expectedDatasetInspectorResult = DatasetInspectorResult(
      expectedDatasetRecords,
      expectedDatasetSize,
      expectedExtraQueryDatasetRecords
    )
    DatasetInspector.inspect(dataset, "") shouldBe expectedDatasetInspectorResult
  }

  test("Should get rename mapping for given messageId if exists") {
    val expectedRenameMapping = sampleColumnRename.map{case (k, v) => k.toLowerCase -> v.toLowerCase}

    DatasetInspector.getColumnRenameMapping(sampleMessageIdForRename) shouldBe expectedRenameMapping
    DatasetInspector.getColumnRenameMapping("id") shouldBe Map.empty
  }

  test("Should keep only rows containing at least one of requested values") {
    import spark.implicits._
    val matchingRows = Seq(
      CustomerTestSchema("id0", "e0"),
      CustomerTestSchema("id1", "e0"),
      CustomerTestSchema("id0", "e3"),
      CustomerTestSchema("id2", "e2"),
    )
    val notMatchingRows = Seq(
      CustomerTestSchema("id3", "e3"),
      CustomerTestSchema("id4")
    )
    val dataset = (matchingRows ++ notMatchingRows).toDS
    val filteredDataset = DatasetInspector.getLoggedDatasetContent(dataset, Map.empty)
    filteredDataset.collect should contain theSameElementsAs matchingRows
  }

  test("Should use extra query for defined messageId") {
    import spark.implicits._
    val matchingRows = Seq(
      CustomerTestSchema("id0", "e0", 11),
      CustomerTestSchema("id101", "e101", 12)
    )
    val notMatchingRows = Seq(
      CustomerTestSchema("id0", "e0", 1),
      CustomerTestSchema("id3", "e3", 3)
    )
    val dataset = (matchingRows ++ notMatchingRows).toDS
    val filteredDataset = DatasetInspector.runExtraQueryIfDefined(dataset, extraQueryId)

    val computedRows = filteredDataset.map(_.as[CustomerTestSchema].collect.toSet).getOrElse(Seq.empty)
    computedRows should contain theSameElementsAs matchingRows.toSet
  }

  test("Should build empty filter for table without matching columns") {
    import spark.implicits._

    val dataset = Seq(NotMatchingSchema("field0"), NotMatchingSchema("field1")).toDS
    val constructedFilter = DatasetInspector.buildMessageFilter(dataset, Map.empty)
    constructedFilter shouldBe lit(false)
  }

  test("Should consider only columns existing in dataset when building a filter") {
    import spark.implicits._

    val dataset = Seq(PartiallyMatchingSchema("id0", Seq("field0")), PartiallyMatchingSchema("id1")).toDS
    val constructedFilter = DatasetInspector.buildMessageFilter(dataset, Map.empty)
    constructedFilter shouldBe (col("customerid") isin (customerIds :_*))
  }

  test("Should consider fields mapping when constricting filters") {
    val fieldMapping = DatasetInspector.getColumnRenameMapping(sampleMessageIdForRename)
    val columnNameToFilter = DatasetInspector.getFilterForLoggedColumns(fieldMapping).map(_._1)

    columnNameToFilter should contain theSameElementsAs Seq("_1.customerId", "eventId").map(_.toLowerCase)
  }

  test("Should consider fields mapping when filtering dataset") {
    import spark.implicits._
    val dataset1 = Seq(
      CustomerTestSchema("id0", "e0"),
      CustomerTestSchema("id2", "e0"),
      CustomerTestSchema("notTrackedId", "e0")
    ).toDS
    val dataset2 = Seq(
      CustomerTestSchema("id0", "e100"),
      CustomerTestSchema("id2", "e1"),
      CustomerTestSchema("notTrackedId", "e200"),
    ).toDS
    val joinedDataset = dataset1.joinWith(dataset2, dataset1("customerId") === dataset2("customerId"))
    val fieldMapping = DatasetInspector.getColumnRenameMapping(sampleMessageIdForRename)

    val expectedLoggedRecords = Seq(
      (CustomerTestSchema("id0", "e0"), CustomerTestSchema("id0", "e100"))
    )
    val computedLoggedRecords = DatasetInspector.getLoggedDatasetContent(joinedDataset, fieldMapping).collect

    computedLoggedRecords should contain theSameElementsAs expectedLoggedRecords
  }
}
