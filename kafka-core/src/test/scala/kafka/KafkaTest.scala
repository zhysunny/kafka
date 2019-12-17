package kafka

import java.io.File

import org.junit._

/**
  * KafkaTest Test.
  * @author 章云
  * @date 2019/12/16 10:38
  */
class KafkaTest {

  @Before
  def before: Unit = {
  }

  @After
  def after: Unit = {
  }

  @Test
  def testGetAbsolutePath: Unit = {
    val file: File = new File("../resources/config/server.properties")
    println(file.getAbsolutePath)
    println(file.exists())
  }

  @Test
  def testGetPropsFromArgs: Unit = {
    val properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties", "--override", "key=value"))
    properties.list(System.out)
    Assert.assertEquals(properties.getProperty("key"), "value")
  }

}

object KafkaTest {
  @BeforeClass
  def beforeClass: Unit = {
    println("Test KafkaTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test KafkaTest Class End...");
  }

  def main(args: Array[String]): Unit = {
    Kafka.main(Array[String]("../resources/config/server.properties"))
  }
}
