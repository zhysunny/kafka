package kafka.utils

import org.junit._

/**
  * ZkUtilsTest Test. 
  * @author 章云
  * @date 2019/12/17 14:08
  */
class ZkUtilsTest {

  val zkUtils: ZkUtils = ZkUtils("localhost:2181", 6000, 6000, false)

  @Before
  def before: Unit = {
  }

  @After
  def after: Unit = {
  }

  //  @Test
  def testGetBrokerSequenceId: Unit = {
    val randomBrokerId = zkUtils.getBrokerSequenceId(1000)
    println(randomBrokerId)
  }

}

object ZkUtilsTest {
  @BeforeClass
  def beforeClass: Unit = {
    println("Test ZkUtilsTest Class Start...");
  }

  @AfterClass
  def afterClass: Unit = {
    println("Test ZkUtilsTest Class End...");
  }
}
