package org.apache.kafka.clients.consumer.internals;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.junit.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.*;

/**
 * SubscriptionState Test
 */
public class SubscriptionStateTest {

    private SubscriptionState subscriptionState;
    private String topic = "test";
    private int partition = 10;
    private List<String> topics;

    @BeforeClass
    public static void beforeClass() throws Exception {
        System.out.println("Test SubscriptionState Class Start...");
    }

    @Before
    public void before() throws Exception {
        subscriptionState = new SubscriptionState(OffsetResetStrategy.LATEST);
        topics = new ArrayList<>();
    }

    @After
    public void after() throws Exception {
    }

    @AfterClass
    public static void afterClass() throws Exception {
        System.out.println("Test SubscriptionState Class End...");
    }

    /**
     * Method: subscribe(List<String> topics, ConsumerRebalanceListener listener)
     */
    @Test
    public void testSubscribeForTopicsListener() throws Exception {
        topics.add(topic);
        subscriptionState.subscribe(topics, new NoOpConsumerRebalanceListener());
        Set<String> subscription = subscriptionState.subscription();
        assertEquals(topics.size(), subscription.size());
        subscription = subscriptionState.groupSubscription();
        assertEquals(topics.size(), subscription.size());
        assertTrue(subscriptionState.partitionAssignmentNeeded());
    }

    /**
     * Method: changeSubscription(List<String> topicsToSubscribe)
     */
    @Test
    public void testChangeSubscription() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: groupSubscribe(Collection<String> topics)
     */
    @Test
    public void testGroupSubscribe() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: needReassignment()
     */
    @Test
    public void testNeedReassignment() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: assignFromUser(Collection<TopicPartition> partitions)
     */
    @Test
    public void testAssignFromUser() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: assignFromSubscribed(Collection<TopicPartition> assignments)
     */
    @Test
    public void testAssignFromSubscribed() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: subscribe(Pattern pattern, ConsumerRebalanceListener listener)
     */
    @Test
    public void testSubscribeForPatternListener() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: hasPatternSubscription()
     */
    @Test
    public void testHasPatternSubscription() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: unsubscribe()
     */
    @Test
    public void testUnsubscribe() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: getSubscribedPattern()
     */
    @Test
    public void testGetSubscribedPattern() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: subscription()
     */
    @Test
    public void testSubscription() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: groupSubscription()
     */
    @Test
    public void testGroupSubscription() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: committed(TopicPartition tp, OffsetAndMetadata offset)
     */
    @Test
    public void testCommittedForTpOffset() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: committed(TopicPartition tp)
     */
    @Test
    public void testCommittedTp() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: needRefreshCommits()
     */
    @Test
    public void testNeedRefreshCommits() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: refreshCommitsNeeded()
     */
    @Test
    public void testRefreshCommitsNeeded() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: commitsRefreshed()
     */
    @Test
    public void testCommitsRefreshed() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: seek(TopicPartition tp, long offset)
     */
    @Test
    public void testSeek() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: assignedPartitions()
     */
    @Test
    public void testAssignedPartitions() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: fetchablePartitions()
     */
    @Test
    public void testFetchablePartitions() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: partitionsAutoAssigned()
     */
    @Test
    public void testPartitionsAutoAssigned() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: position(TopicPartition tp, long offset)
     */
    @Test
    public void testPositionForTpOffset() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: position(TopicPartition tp)
     */
    @Test
    public void testPositionTp() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: allConsumed()
     */
    @Test
    public void testAllConsumed() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: needOffsetReset(TopicPartition partition, OffsetResetStrategy offsetResetStrategy)
     */
    @Test
    public void testNeedOffsetResetForPartitionOffsetResetStrategy() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: needOffsetReset(TopicPartition partition)
     */
    @Test
    public void testNeedOffsetResetPartition() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: hasDefaultOffsetResetPolicy()
     */
    @Test
    public void testHasDefaultOffsetResetPolicy() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isOffsetResetNeeded(TopicPartition partition)
     */
    @Test
    public void testIsOffsetResetNeeded() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: resetStrategy(TopicPartition partition)
     */
    @Test
    public void testResetStrategy() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: hasAllFetchPositions()
     */
    @Test
    public void testHasAllFetchPositions() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: missingFetchPositions()
     */
    @Test
    public void testMissingFetchPositions() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: partitionAssignmentNeeded()
     */
    @Test
    public void testPartitionAssignmentNeeded() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isAssigned(TopicPartition tp)
     */
    @Test
    public void testIsAssigned() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isPaused(TopicPartition tp)
     */
    @Test
    public void testIsPaused() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: isFetchable(TopicPartition tp)
     */
    @Test
    public void testIsFetchable() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: pause(TopicPartition tp)
     */
    @Test
    public void testPause() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: resume(TopicPartition tp)
     */
    @Test
    public void testResume() throws Exception {
        //TODO: Test goes here...
    }

    /**
     * Method: listener()
     */
    @Test
    public void testListener() throws Exception {
        //TODO: Test goes here...
    }

}
