# Dataset logger example

`CustomerReport` presents a simple usage of `DatasetLogger` in a Spark application. Generated tables are represented by Datasets 
with debug log message after each transformation. As `logDataset` method is called inside `logger.debug`, Scala macros
ensure that it is invoked only when application logging level is set to DEBUG. Such composition avoids invoking Spark actions
(`count` and `collect`) on Datasets passed to `logDataset` in non-debug execution, it doesn't affect test coverage as well. 
This is a recommended configuration allowing to leverage `DatasetLogger` features without adding any  computation overhead 
for Spark jobs.

In this basic example `DatasetLogger` configuration is passed through package's `resources`. This solution is not well-fitted 
for executing distributed Spark applications, but works well for tool presentation. In real world scenario it's more convenient
to pass a filepath or string with JSON content as e.g. a `spark-submit` parameter.

To run the application, execute `sbt run &> log.txt` command - by running a Spark app with sbt, all the debug log messages
are produced, hence forwarding the output to `log.txt` file allows to parse the  job's output and get only required lines.
After the execution run `grep "DatasetLogger" log.txt ` to get the `DatasetLogger` content. The result for default config
from [resources/config.json](src/main/resources/config.json) that tracks few `customerId` and `orderId` values, and performs
a simple aggregation for `logDataset` marked with `messageId = 4` looks like that:

```shell
> grep "DatasetLogger" log.txt 

[DatasetLogger] Generated Customers dataset
[DatasetLogger] ----------------
[DatasetLogger][customers] Dataset contains 5 matching rows:
[DatasetLogger] ----------------
[DatasetLogger] | customerId -> 1 | name -> Bilbo Baggins | age -> 10 | country -> US |
[DatasetLogger] | customerId -> 2 | name -> Bilbo Baggins | age -> 76 | country -> ES |
[DatasetLogger] | customerId -> 3 | name -> John Doe | age -> 77 | country -> IT |
[DatasetLogger] | customerId -> 4 | name -> John Doe | age -> 38 | country -> US |
[DatasetLogger] | customerId -> 5 | name -> Draco Malfoy | age -> 25 | country -> ES |
[DatasetLogger]
[DatasetLogger] Generated Products dataset
[DatasetLogger] ----------------
[DatasetLogger][products] Dataset contains 0 matching rows:
[DatasetLogger] ----------------
[DatasetLogger]
[DatasetLogger] Generated Orders dataset
[DatasetLogger] ----------------
[DatasetLogger][orders] Dataset contains 4 matching rows:
[DatasetLogger] ----------------
[DatasetLogger] | orderId -> 7 | customerId -> 31 | productId -> 16 |
[DatasetLogger] | orderId -> 8 | customerId -> 26 | productId -> 1 |
[DatasetLogger] | orderId -> 17 | customerId -> 3 | productId -> 8 |
[DatasetLogger] | orderId -> 23 | customerId -> 2 | productId -> 39 |
[DatasetLogger]
[DatasetLogger] Joined orders with customers
[DatasetLogger] ----------------
[DatasetLogger][join1] Dataset contains 4 matching rows:
[DatasetLogger] ----------------
[DatasetLogger] | _1.orderId -> 23 | _1.customerId -> 2 | _2.name -> Bilbo Baggins | _1.productId -> 39 | _2.age -> 76 | _2.country -> ES | _2.customerId -> 2 |
[DatasetLogger] | _1.orderId -> 17 | _1.customerId -> 3 | _2.name -> John Doe | _1.productId -> 8 | _2.age -> 77 | _2.country -> IT | _2.customerId -> 3 |
[DatasetLogger] | _1.orderId -> 8 | _1.customerId -> 26 | _2.name -> John Doe | _1.productId -> 1 | _2.age -> 58 | _2.country -> FR | _2.customerId -> 26 |
[DatasetLogger] | _1.orderId -> 7 | _1.customerId -> 31 | _2.name -> Bart Simpson | _1.productId -> 16 | _2.age -> 10 | _2.country -> ES | _2.customerId -> 31 |
[DatasetLogger] ----------------
[DatasetLogger] ----- EXTRA SQL QUERY RESULT -----
[DatasetLogger] ----------------
[DatasetLogger] | order_country -> IT | order_num -> 10 |
[DatasetLogger] | order_country -> FR | order_num -> 10 |
[DatasetLogger] | order_country -> ES | order_num -> 8 |
[DatasetLogger] | order_country -> PL | order_num -> 6 |
[DatasetLogger] | order_country -> US | order_num -> 6 |
[DatasetLogger]
[DatasetLogger] Joined orders with customers and products
[DatasetLogger] ----------------
[DatasetLogger][join2] Dataset contains 4 matching rows:
[DatasetLogger] ----------------
[DatasetLogger] | _1.orderId -> 23 | _1.customerId -> 2 | _2.name -> Bilbo Baggins | _3.weight -> 4.60849445419997 | _3.productId -> 39 | _1.productId -> 39 | _2.age -> 76 | _3.name -> Game Console | _2.country -> ES | _2.customerId -> 2 |
[DatasetLogger] | _1.orderId -> 17 | _1.customerId -> 3 | _2.name -> John Doe | _3.weight -> 2.614371534907635 | _3.productId -> 8 | _1.productId -> 8 | _2.age -> 77 | _3.name -> Game Console | _2.country -> IT | _2.customerId -> 3 |
[DatasetLogger] | _1.orderId -> 8 | _1.customerId -> 26 | _2.name -> John Doe | _3.weight -> 9.758419282052438 | _3.productId -> 1 | _1.productId -> 1 | _2.age -> 58 | _3.name -> Game Console | _2.country -> FR | _2.customerId -> 26 |
[DatasetLogger] | _1.orderId -> 7 | _1.customerId -> 31 | _2.name -> Bart Simpson | _3.weight -> 4.068152493536417 | _3.productId -> 16 | _1.productId -> 16 | _2.age -> 10 | _3.name -> Notebook | _2.country -> ES | _2.customerId -> 31 |
[DatasetLogger]
```
