package io.datumo.logger

import org.apache.spark.sql.types.StructType

object DatasetTransforms {

  def flattenSchema(schema: StructType, prefix: Option[String] = None): Seq[String] =
    schema.fields.flatMap { field =>
      val colName = prefix.map(_ + ".").orEmpty + field.name

      field.dataType match {
        case struct: StructType => flattenSchema(struct, Some(colName))
        case _                  => Seq(colName)
      }
    }

}
