package kafka.metrics

import kafka.Kafka
import kafka.utils.VerifiableProperties
import org.junit._

/**
  * KafkaMetricsConfigTest Test. 
  * @author 章云
  * @date 2019/12/16 15:24
  */
class KafkaMetricsConfigTest {

  @Before
  def before: Unit = {
  }

  @After
  def after: Unit = {
  }

  @Test
  def testHasConfig: Unit = {
    val properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties", "--override", "kafka.metrics.reporters=reporter1,reporter2", "--override", "kafka.metrics.polling.interval.secs=20"))
    val vp: VerifiableProperties = new VerifiableProperties(properties)
    val kmc: KafkaMetricsConfig = new KafkaMetricsConfig(vp)
    val reporters = properties.getProperty("kafka.metrics.reporters").split(",").toList
    kmc.reporters.foreach(str => {
      Assert.assertTrue(reporters.contains(str))
    })
    val interval = properties.getProperty("kafka.metrics.polling.interval.secs").toInt
    Assert.assertEquals(interval, kmc.pollingIntervalSecs)
  }

  @Test
  def testDefaultConfig: Unit = {
    val properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties"))
    val vp: VerifiableProperties = new VerifiableProperties(properties)
    val kmc: KafkaMetricsConfig = new KafkaMetricsConfig(vp)
    Assert.assertEquals(kmc.reporters.size, 0)
    Assert.assertEquals(kmc.pollingIntervalSecs, 10)
  }

}

object KafkaMetricsConfigTest {
  @BeforeClass
  def beforeClass: Unit = {
    println("Test KafkaMetricsConfigTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test KafkaMetricsConfigTest Class End...");
  }
}
