package io.datumo.logger

import org.scalatest.matchers.should.Matchers

class DatasetTransformsSpec extends SparkFunSuite with Matchers {
  test("Should flatten nested schema column names") {
    import spark.implicits._
    val dataset = Seq(NestedSchema("A", PartiallyMatchingSchema("B",Seq("A", "C")))).toDS
    val columnNames = Seq("topLevelField", "nestedField.customerId", "nestedField.notMatchingField")
    val computedFlattenColumns = DatasetTransforms.flattenSchema(dataset.schema)
    computedFlattenColumns should contain theSameElementsAs columnNames
  }
}
