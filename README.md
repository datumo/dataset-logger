# Dataset Logger

`DatasetLogger` allows to investigate Spark Dataset (and DataFrame) content during job execution without messing the code
or performance. With configuration provided on launch you can track specific records and investigate tables at
different stages of your application.

> Customer with ID `10101` have value `12.34` instead of `43.21` in field `X` and customer `98989` is not available in the output

that's kind of message we all hate to hear. `DatasetLogger` was designed to address such investigation problems - it's
capable of tracking particular records in any Dataset/DataFrame in your Spark app without any extra development and deployment.
All you need to do is to launch your job with configuration specifying what values should be logged during execution - 
e.g. all the records with `customerId` in `[10101, 98989]`. 

## Installation

## Usage

Extend your class with `DatasetLogger`, or initialize an instance of it, to use its features. `DatasetLogger` takes single parameter - either `String` or 
`Option[String]` containing logger's configuration in JSON format. [circe](https://github.com/circe/circe) library is used
for parsing.

Logger should be invoked on demand, with specific configuration for each usage. You may pass it as an optional `spark-submit`
command line argument or load it from a file, transformed to a Scala object (`String` or `Option[String]`) at the beginning
of application's execution. Afterwards it should be used to initialize `DatasetLogger`.

`logDataset` method is an entry point for the whole magic. It takes a Dataset/DataFrame as one of the arguments and filters
it with columns/values specified in the configuration. You may also specify a SQL query that will be executed on your data,
with `dataset` denoted as the table name.

### Avoiding evaluation in regular execution

It's recommended to call `logDataset` inside `logger.debug()` method of your job's logger. Thanks to Scala macros used by most
logging libraries, the debug messages are evaluated only when they are invoked during application execution with logging 
level set to DEBUG. Therefore, expensive Spark actions needed to collect and present the data by `DatasetLogger` are not
executed during regular app run. The Scala macros exclude these lines from test coverage statistics. 

Check [example job](examples/customer-report) for JSON with configuration and class' usage reference.

### Parameters

| parameter | type | required | description |
|--|--|--|--|
| message | `String` | yes | log message to be printed at the beginning, it's also the only content produced when configuration is not passed.
| dataset | `Dataset[A]` | yes | Spark `Dataset`/`DataFrame` to be inspected.
| messageId | `String` | yes | allows identifying specific `logDataset` call in your job, so you can specify some extra actions for particular step.
| maxRowsNum | `Int` | no | how many records can be logged on the output. Default: 100
| cacheLevel | `Option[StorageLevel]` | no | specifies whether the Dataset should be persisted and if so - which [storage level](https://spark.apache.org/docs/3.3.2/rdd-programming-guide.html#which-storage-level-to-choose) should be used for caching the data. Default: `Some(StorageLevel.MEMORY_AND_DISK)`

### Performance consideration - caching the Dataset/DataFrame

The last, optional argument - `cacheLevel: Option[StorageLevel]` specifies how the passed `dataset` should be cached. As computations
performed within `logDataset` contain Spark actions (collecting matching records, counting their size, running extra query) 
and most probably `dataset` is used by further transformations, the Dataset/DataFrame should be cached to avoid repeated computations.
`cacheLevel` specifies storage approach used for caching (`MEMORY_AND_DISK` by default). If for some reason you prefer not to
cache `dataset`, you should set this argument to `None`.
```scala
val notCachedDf: DataFrame = ...
logger.debug(
  logDataset("Constructed dataset that shouldn't be cached", notCachedDf, "not_cached", cacheLevel = None)
)

val diskOnlyCachedDf: DataFrame = ...
logger.debug(
  logDataset("Constructed dataset that should be cached on disk only",  diskOnlyCachedDf, "disk_cached", cacheLevel = Some(DISK_ONLY))
)
```

### Logger configuration

The JSON with `DatasetLogger` configuration should contain following params:
 - `columnFiltersConfig` - list of objects representing column and its values used to filter a Dataset/DataFrame. Each object
should contain two key/value pairs - `columnName` with a string specifying the column and `columnValues` - list of strings
representing values used for table's content filtering. Example:
```json
"columnFiltersConfig": [
  {
    "columnName": "customerId",
    "columnValues": ["123", "456"]
  }
]
```
- `extraQuery` (optional) - mapping/object that allows to run a SQL query on the Dataset/DataFrame. `dataset` is the name 
used to denote your table. The query (value in `extraQuery`) is executed during `logDataset` call with specified `messageId` 
passed as `extraQuery` key. Example of query executed for `logDataset` with `messageId = "customerCountries""`:
```json
"extraQuery": {
  "customerCountries": "SELECT customerType, COUNT(*) FROM dataset WHERE customerCountry IN ['PL', 'US'] GROUP BY customerType"
}
```
- `logOnly` (optional) - List of `messageId` values that should be the only invoked `logDataset` calls (for all the others
only `message` is displayed). When not specified all the method's call are fully evaluated. `logOnly` is especially useful
when you have a lot of `logDataset` calls, but you are interested only in a specific step of the job, so executing all the
others (each of them taking some time to run Spark actions) is redundant.
```json
"logOnly": ["id1", "id14", "id18"]
```
- `columnRenameMapping` (optional) - Useful when schema of the Dataset/DataFrame evolves (e.g. after joining Datasets or
performing some `.select`/`.withColumnRenamed` on a DataFrame) and `columnName` specified in `columnFiltersConfig` is not
valid on particular stage. Let's say that in the `columnFiltersConfig` you configure to track `customerId` column during
your job. You use Dataset API and at some point you join two of them with `val dataset3 = dataset1.joinWith(dataset2, ...)`.
If you pass `dataset3` to `logDataset` then you won't be able to find `customerId` column in it. However, `dataset3` has
this value denoted as e.g. `_1.customerId`. Similar case could happen when your job uses DataFrame API and you call
`val df2 = df1.select($"customerId" as "cId", ...)` - to track `customerId` values for `df2` `logDataset` needs to filter on
`cId` column. `columnRenameMapping` is meant to specify such column name changes for `logDataset` specified with particular
`messageId`. Example:
```json
"columnRenameMapping": {
  "orders": {
    "customerId": "cId",
    "orderId": "oId"
  },
  "delivery": {
    "customerId": "cId",
    "orderId": "orderFinalId"
  }
}
```
