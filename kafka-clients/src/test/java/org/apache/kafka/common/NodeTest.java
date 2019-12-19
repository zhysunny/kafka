package org.apache.kafka.common;

import static org.junit.Assert.*;
import org.junit.*;

/**
 * Node Test.
 * @author 章云
 * @date 2019/12/19 14:18
 */
public class NodeTest {

    /**
     * 两个不同的实例
     */
    private Node node1 = Node.noNode();
    private Node node2 = Node.noNode();

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test Node Class Start...");
    }

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test Node Class End...");
    }

    @Test
    public void testGetMethod() throws Exception {
        assertEquals(node1.id(), -1);
        assertEquals(node1.idString(), "-1");
        assertEquals(node1.host(), "");
        assertEquals(node1.port(), -1);
    }

    /**
     * Method: hashCode()
     */
    @Test
    public void testHashCode() throws Exception {
        assertEquals(node1.hashCode(), node2.hashCode());
    }

    /**
     * Method: equals(Object obj)
     */
    @Test
    public void testEquals() throws Exception {
        assertEquals(node1, node2);
    }

    /**
     * Method: toString()
     */
    @Test
    public void testToString() throws Exception {
        System.out.println(node1.toString());
    }

} 
