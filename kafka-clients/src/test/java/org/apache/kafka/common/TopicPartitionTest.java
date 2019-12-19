package org.apache.kafka.common;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * TopicPartition Test.
 * @author 章云
 * @date 2019/12/19 14:37
 */
public class TopicPartitionTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test TopicPartition Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test TopicPartition Class End...");
    }

    @Test
    public void testInstance() throws Exception {
        TopicPartition topic1 = new TopicPartition("topic", 10);
        TopicPartition topic2 = new TopicPartition("topic", 10);
        assertEquals(topic1.topic(), "topic");
        assertEquals(topic1.partition(), 10);
        assertEquals(topic1, topic2);
        assertEquals(topic1.hashCode(), topic2.hashCode());
        System.out.println(topic1.toString());
    }

}
