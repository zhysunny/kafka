package org.apache.kafka.clients.producer.internals;

import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.Method;

/**
 * DefaultPartitioner Test.
 * @author 章云
 * @date 2020/1/16 10:52
 */
public class DefaultPartitionerTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test DefaultPartitioner Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test DefaultPartitioner Class End...");
    }

    /**
     * Method: partition(String topic, byte[] keyBytes, Cluster cluster)
     */
    @Test
    public void testPartition() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: toPositive(int number)
     */
    @Test
    public void testToPositive() throws Exception {
        for (int i = 0; i > -10; i--) {
            System.out.println(i + "===" + toPositive(i));
        }
    }

    public int toPositive(int number) throws Exception {
        try {
            Method method = DefaultPartitioner.class.getDeclaredMethod("toPositive", int.class);
            method.setAccessible(true);
            return (int)method.invoke(DefaultPartitioner.class, new Integer[]{ number });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

} 
