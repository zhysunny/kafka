package org.apache.kafka.common;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * PartitionInfo Test.
 * @author 章云
 * @date 2019/12/19 14:14
 */
public class PartitionInfoTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test PartitionInfo Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test PartitionInfo Class End...");
    }

    /**
     * Method: topic()
     */
    @Test
    public void testInstance() throws Exception {
        Node leader = Node.noNode();
        Node followers1 = Node.noNode();
        Node followers2 = Node.noNode();
        Node[] replicas = { followers1, followers2 };
        Node[] inSyncReplicas = { followers1, followers2 };
        PartitionInfo partitionInfo = new PartitionInfo("topic", 0, leader, replicas, inSyncReplicas);
        assertEquals(partitionInfo.topic(), "topic");
        assertEquals(partitionInfo.partition(), 0);
        assertEquals(partitionInfo.leader(), leader);
        assertArrayEquals(partitionInfo.replicas(), replicas);
        assertArrayEquals(partitionInfo.inSyncReplicas(), inSyncReplicas);
        System.out.println(partitionInfo.toString());
    }

}
