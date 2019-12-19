package org.apache.kafka.clients.producer;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * ProducerRecord Test.
 * @author 章云
 * @date 2019/12/19 10:04
 */
public class ProducerRecordTest {

    ProducerRecord<String, String> record = new ProducerRecord<>("topic", 0, "key", "value");
    ProducerRecord<String, String> record0 = new ProducerRecord<>("topic", 0, "key", "value");
    ProducerRecord<String, String> record1 = new ProducerRecord<>("topic", 1, "key", "value");

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test ProducerRecord Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test ProducerRecord Class End...");
    }

    @Test
    public void testInstance() throws Exception {
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("topic", 0, "key", "value");
        producerRecord = new ProducerRecord<>("topic", "key", "value");
        producerRecord = new ProducerRecord<>("topic", "value");
    }

    /**
     * Method: topic()
     */
    @Test
    public void testTopic() throws Exception {
        assertEquals(record.topic(), "topic");
    }

    /**
     * Method: key()
     */
    @Test
    public void testKey() throws Exception {
        assertEquals(record.key(), "key");
    }

    /**
     * Method: value()
     */
    @Test
    public void testValue() throws Exception {
        assertEquals(record.value(), "value");
    }

    /**
     * Method: partition()
     */
    @Test
    public void testPartition() throws Exception {
        assertEquals(record.partition().intValue(), 0);
    }

    /**
     * Method: equals(Object o)
     */
    @Test
    public void testEquals() throws Exception {
        assertTrue(record.equals(record0));
        assertFalse(record.equals(record1));
    }

    /**
     * Method: hashCode()
     */
    @Test
    public void testHashCode() throws Exception {
        assertEquals(record.hashCode(), record0.hashCode());
        assertNotSame(record.hashCode(), record1.hashCode());
    }

} 
