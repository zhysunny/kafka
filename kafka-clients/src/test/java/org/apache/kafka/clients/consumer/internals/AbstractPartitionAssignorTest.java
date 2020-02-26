package org.apache.kafka.clients.consumer.internals;

import static org.junit.Assert.*;
import org.apache.kafka.clients.consumer.RangeAssignor;
import org.apache.kafka.clients.consumer.RoundRobinAssignor;
import org.apache.kafka.common.TopicPartition;
import org.junit.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractPartitionAssignor Test.
 */
public class AbstractPartitionAssignorTest {

    private AbstractPartitionAssignor range = new RangeAssignor();
    private AbstractPartitionAssignor roundRobin = new RoundRobinAssignor();
    private Map<String, Integer> partitionsPerTopic;
    private Map<String, List<String>> subscriptions;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test AbstractPartitionAssignor Class Start...");
    }

    @Before
    public void before() throws Exception {
        //模拟消费者和主题和分区数
        // topic
        partitionsPerTopic = new HashMap<>();
        partitionsPerTopic.put("topic-0", 11);
        partitionsPerTopic.put("topic-1", 11);
        partitionsPerTopic.put("topic-2", 11);
        // 消费者，假设每个消费者都绑定上面的主题
        subscriptions = new HashMap<>();
        List<String> topics = new ArrayList<>(partitionsPerTopic.keySet());
        subscriptions.put("consumer-0", topics);
        subscriptions.put("consumer-1", topics);
        subscriptions.put("consumer-2", topics);
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test AbstractPartitionAssignor Class End...");
    }

    /**
     * Method: assign(Map<String, Integer> partitionsPerTopic, Map<String, List<String>> subscriptions)
     */
    @Test
    public void testRangeAssign() throws Exception {
        Map<String, List<TopicPartition>> assign = range.assign(partitionsPerTopic, subscriptions);
        // 每个topic 11个分区，分3个消费者，每个消费者分区数分别是(4,4,3)，3个topic分配就是3*(4,4,3)=(12,12,9)，这种情况下容易出现数据倾斜
        for (Map.Entry<String, List<TopicPartition>> entry : assign.entrySet()) {
            String consumer = entry.getKey();
            List<TopicPartition> tps = entry.getValue();
            System.out.println(consumer + " === " + tps);
        }
    }

    /**
     * Method: assign(Map<String, Integer> partitionsPerTopic, Map<String, List<String>> subscriptions)
     */
    @Test
    public void testRoundRobinAssign() throws Exception {
        Map<String, List<TopicPartition>> assign = roundRobin.assign(partitionsPerTopic, subscriptions);
        // 将3个topic所有分区组成列表，供消费者轮询分配
        for (Map.Entry<String, List<TopicPartition>> entry : assign.entrySet()) {
            String consumer = entry.getKey();
            List<TopicPartition> tps = entry.getValue();
            System.out.println(consumer + " === " + tps);
        }
    }

}
