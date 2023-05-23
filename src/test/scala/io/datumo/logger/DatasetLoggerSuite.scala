package io.datumo.logger

final case class CustomerTestSchema(customerId: String = "test_cid", eventId: String = "test_eid", extraValue: Int = 0)
final case class NotMatchingSchema(notMatchingField: String = "test_placeholder")
final case class PartiallyMatchingSchema(customerId: String = "test_cid", notMatchingField: Seq[String] = Seq.empty)
final case class NestedSchema(topLevelField: String = "top_level", nestedField: PartiallyMatchingSchema = PartiallyMatchingSchema())

object DatasetLoggerSuite {
  val customerIds = Seq("id0", "id1")
  val eventIds = Seq("e0", "e1", "e2")
  val extraQueryId = "extraQuery"
  val loggedColumnValuesConfig = Seq(
    LoggedColumnConfig("customerId", customerIds),
    LoggedColumnConfig("eventId", eventIds)
  )
  val extraQueries = Map(extraQueryId -> s"SELECT * FROM $ExtraQueryDatasetName WHERE extraValue > 10")

  val sampleColumnRename = Map("customerId" -> "_1.customerId")
  val sampleMessageIdForRename = "log3"
  val columnRenameMapping = Map(sampleMessageIdForRename -> sampleColumnRename)

  val loggerConfig = DatasetLoggerConfig(
    loggedColumnValuesConfig,
    Some(extraQueries),
    columnRenameMapping = Some(columnRenameMapping)
  )
  implicit  val implicitLoggerConfig: DatasetLoggerConfig = loggerConfig

  val datasetLogger = new DatasetLogger(None) {
    override lazy val datasetLoggerConfig = Some(loggerConfig)
  }

  def customerToRecordFields(customer: CustomerTestSchema): RecordFields =
    Map(
      "customerId" -> customer.customerId,
      "eventId" -> customer.eventId,
      "extraValue" -> customer.extraValue
    )
}
