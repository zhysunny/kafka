package org.apache.kafka.clients.producer;

import static org.junit.Assert.*;
import org.junit.*;
import java.util.Properties;

/**
 * KafkaProducer Test.
 * @author 章云
 * @date 2019/12/19 10:30
 */
public class KafkaProducerTest {

    private Properties props = new Properties();

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test KafkaProducer Class Start...");
    }

    @Before
    public void before() throws Exception {
        props.put("bootstrap.servers", "10.45.157.216:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test KafkaProducer Class End...");
    }

    @Test
    public void testInstance() throws Exception {
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);
    }

    /**
     * Method: send(ProducerRecord<K, V> record)
     */
    @Test
    public void testSendRecord() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: send(ProducerRecord<K, V> record, Callback callback)
     */
    @Test
    public void testSendForRecordCallback() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: flush()
     */
    @Test
    public void testFlush() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: partitionsFor(String topic)
     */
    @Test
    public void testPartitionsFor() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: metrics()
     */
    @Test
    public void testMetrics() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: close()
     */
    @Test
    public void testClose() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: close(long timeout, TimeUnit timeUnit)
     */
    @Test
    public void testCloseForTimeoutTimeUnit() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: cancel(boolean interrupt)
     */
    @Test
    public void testCancel() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: get()
     */
    @Test
    public void testGet() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: get(long timeout, TimeUnit unit)
     */
    @Test
    public void testGetForTimeoutUnit() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isCancelled()
     */
    @Test
    public void testIsCancelled() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isDone()
     */
    @Test
    public void testIsDone() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: parseAcks(String acksString)
     */
    @Test
    public void testParseAcks() throws Exception {
        //TODO: Test goes here...
/* 
try { 
   Method method = KafkaProducer.getClass().getMethod("parseAcks", String.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: waitOnMetadata(String topic, long maxWaitMs)
     */
    @Test
    public void testWaitOnMetadata() throws Exception {
        //TODO: Test goes here...
/* 
try { 
   Method method = KafkaProducer.getClass().getMethod("waitOnMetadata", String.class, long.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: ensureValidRecordSize(int size)
     */
    @Test
    public void testEnsureValidRecordSize() throws Exception {
        //TODO: Test goes here...
/* 
try { 
   Method method = KafkaProducer.getClass().getMethod("ensureValidRecordSize", int.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

    /**
     * Method: partition(ProducerRecord<K, V> record, byte[] serializedKey, byte[] serializedValue, Cluster cluster)
     */
    @Test
    public void testPartition() throws Exception {
        //TODO: Test goes here...
/* 
try { 
   Method method = KafkaProducer.getClass().getMethod("partition", ProducerRecord<K,.class, byte[].class, byte[].class, Cluster.class); 
   method.setAccessible(true); 
   method.invoke(<Object>, <Parameters>); 
} catch(NoSuchMethodException e) { 
} catch(IllegalAccessException e) { 
} catch(InvocationTargetException e) { 
} 
*/
    }

} 
