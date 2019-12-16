package kafka.utils

import kafka.Kafka
import org.junit._

/**
  * VerifiablePropertiesTest Test. 
  * @author 章云
  * @date 2019/12/16 11:01
  */
class VerifiablePropertiesTest {

  val properties = Kafka.getPropsFromArgs(Array[String]("../resources/config/server.properties"))
  val vp: VerifiableProperties = new VerifiableProperties(properties)

  @Before
  def before: Unit = {
    println(vp)
  }

  @After
  def after: Unit = {
  }

  @Test
  def testContainsKey: Unit = {
    Assert.assertFalse(vp.containsKey("key"))
    Assert.assertTrue(vp.containsKey("log.dirs"))
  }

  @Test
  def testGetProperty: Unit = {
    val value = vp.getProperty("key")
    Assert.assertNull(value)
    val logDirs = vp.getProperty("log.dirs")
    Assert.assertNotNull(logDirs)
  }

  @Test
  def test: Unit = {
    vp.getProperty("log.dirs")
    vp.verify()
  }

}

object VerifiablePropertiesTest {

  @BeforeClass
  def beforeClass: Unit = {
    println("Test VerifiablePropertiesTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test VerifiablePropertiesTest Class End...");
  }
}
