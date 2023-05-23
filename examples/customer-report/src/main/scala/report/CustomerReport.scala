package report

import scala.util.Random
import com.typesafe.scalalogging.Logger
import io.datumo.logger.DatasetLogger
import org.apache.spark.sql.{Dataset, SparkSession}
import org.apache.spark.storage.StorageLevel

final case class Customer(customerId: Int, name: String, age: Int, country: String)
final case class Product(productId: Int, name: String, weight: Double)
final case class Order(orderId: Int, customerId: Int, productId: Int)

class CustomerReport(config: String) extends DatasetLogger(config) {
  private val logger = Logger(getClass.getName)

  def execute(numCustomers: Int, numProducts: Int, numOrders: Int)(implicit spark: SparkSession) = {
    import spark.implicits._

    val customers = generateCustomers(numCustomers)
    logger.debug(logDataset("Generated Customers dataset", customers.cache(), "customers"))
    val products = generateProducts(numProducts)
    logger.debug(logDataset("Generated Products dataset", products.cache(), "products"))
    val orders = generateOrders(numOrders, numCustomers, numProducts)
    logger.debug(logDataset("Generated Orders dataset", orders.cache(), "orders"))
    val ordersWithCustomers = orders.joinWith(customers, orders("customerId") === customers("customerId"))
    logger.debug(logDataset("Joined orders with customers", ordersWithCustomers, "join1"))
    val ordersWithCustomersAndProduct =
      ordersWithCustomers
        .joinWith(products, products("productId") === ordersWithCustomers("_1.productId"))
        .map { case ((order, customer), product) => (order, customer, product) }
    logger.debug(
      logDataset(
        "Joined orders with customers and products",
        ordersWithCustomersAndProduct,
        "join2",
        cacheLevel = Some(StorageLevel.MEMORY_ONLY)
      )
    )
    ordersWithCustomersAndProduct
  }

  private[report] def generateCustomers(recordsNum: Int)(implicit spark: SparkSession): Dataset[Customer] = {
    import spark.implicits._

    val names = Seq("John Doe", "Bart Simpson", "Luke Skywalker", "Draco Malfoy", "Bilbo Baggins")
    val countries = Seq("US", "PL", "FR", "IT", "ES")
    (1 to recordsNum).toList.map { i =>
      Customer(i, names(Random.nextInt(names.size)), Random.nextInt(100), countries(Random.nextInt(countries.size)))
    }.toDS
  }

  private[report] def generateProducts(recordsNum: Int)(implicit spark: SparkSession): Dataset[Product] = {
    import spark.implicits._

    val names = Seq("Notebook", "Mobile", "TV", "Game Console")
    (1 to recordsNum).toList.map { i =>
      Product(i, names(Random.nextInt(names.size)), Random.nextDouble() * 10)
    }.toDS
  }

  private[report] def generateOrders(recordsNum: Int, numCustomers: Int, numProducts: Int)(implicit
    spark: SparkSession
  ): Dataset[Order] = {
    import spark.implicits._
    (1 to recordsNum).toList.map { i =>
      Order(i, Random.nextInt(numCustomers), Random.nextInt(numProducts))
    }.toDS
  }
}

object RunReport {
  private def readJsonFromFile(path: String): String = {
    val jsonSource = scala.io.Source.fromResource(path)
    try jsonSource.mkString
    finally jsonSource.close
  }

  def main(args: Array[String]): Unit = {

    implicit val spark = SparkSession.builder
      .master("local[*]")
      .appName("Customer Report")
      .getOrCreate()

    val config = readJsonFromFile("config.json")

    val report = new CustomerReport(config)
    val recordsNum = 42
    report.execute(recordsNum, recordsNum, recordsNum)

  }
}
