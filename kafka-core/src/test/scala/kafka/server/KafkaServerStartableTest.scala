package kafka.server

import java.util.Properties

import kafka.Kafka
import org.junit._

/**
  * KafkaServerStartableTest Test.
  * @author 章云
  * @date 2019/12/16 10:57
  */
class KafkaServerStartableTest {

  @Before
  def before: Unit = {
  }

  @After
  def after: Unit = {
  }

  @Test
  def testInstance: Unit = {
    val properties: Properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties"))
    val config: KafkaConfig = KafkaConfig.fromProps(properties)
    val startable = new KafkaServerStartable(config)
    startable.setServerState(Starting.state)
    //    startable.startup()  // 启动
    //    startable.awaitShutdown()  // 等待
    //    startable.shutdown()  // 停止
  }

}

object KafkaServerStartableTest {
  @BeforeClass
  def beforeClass: Unit = {
    println("Test KafkaServerStartableTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test KafkaServerStartableTest Class End...");
  }
}
