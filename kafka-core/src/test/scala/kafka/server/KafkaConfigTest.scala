package kafka.server

import java.util.Properties

import kafka.Kafka
import org.junit._

import scala.collection.JavaConversions

/**
  * KafkaConfigTest Test. 
  * @author 章云
  * @date 2019/12/16 15:48
  */
class KafkaConfigTest {

  @Before
  def before: Unit = {
  }

  @After
  def after: Unit = {
  }

  @Test
  def testInstance: Unit = {
    val properties: Properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties"))
    KafkaConfig.fromProps(properties)
    KafkaConfig.fromProps(properties, true)
    KafkaConfig.fromProps(new Properties(), properties)
    KafkaConfig.fromProps(new Properties(), properties, true)
    new KafkaConfig(properties, true)
    val props = new java.util.HashMap[String, String]()
    JavaConversions.asScalaSet(properties.entrySet()).foreach(entry => props.put(entry.getKey.toString, entry.getValue.toString))
    KafkaConfig.apply(props)
  }

}

object KafkaConfigTest {
  @BeforeClass
  def beforeClass: Unit = {
    println("Test KafkaConfigTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test KafkaConfigTest Class End...");
  }
}
