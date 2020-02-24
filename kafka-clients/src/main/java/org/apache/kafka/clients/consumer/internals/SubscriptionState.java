/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.kafka.clients.consumer.internals;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.common.TopicPartition;
import java.util.*;
import java.util.regex.Pattern;

/**
 * A class for tracking the topics, partitions, and offsets for the consumer. A partition
 * is "assigned" either directly with {@link #assignFromUser(Collection)} (manual assignment)
 * or with {@link #assignFromSubscribed(Collection)} (automatic assignment from subscription).
 * <p>
 * Once assigned, the partition is not considered "fetchable" until its initial position has
 * been set with {@link #seek(TopicPartition, long)}. Fetchable partitions track a fetch
 * position which is used to set the offset of the next fetch, and a consumed position
 * which is the last offset that has been returned to the user. You can suspend fetching
 * from a partition through {@link #pause(TopicPartition)} without affecting the fetched/consumed
 * offsets. The partition will remain unfetchable until the {@link #resume(TopicPartition)} is
 * used. You can also query the pause state independently with {@link #isPaused(TopicPartition)}.
 * <p>
 * Note that pause state as well as fetch/consumed positions are not preserved when partition
 * assignment is changed whether directly by the user or through a group rebalance.
 * <p>
 * This class also maintains a cache of the latest commit position for each of the assigned
 * partitions. This is updated through {@link #committed(TopicPartition, OffsetAndMetadata)} and can be used
 * to set the initial fetch position (e.g. {@link Fetcher#resetOffset(TopicPartition)}.
 */

/**
 * 三种消费者指定方式
 * 1.subscribe(List topics)自动分配分区
 * 2.subscribe(Pattern pattern)模式匹配,自动分配分区
 * 3.assign()指定分区
 * @author 章云
 * @date 2020/2/17 8:57
 */
public class SubscriptionState {

    /* the pattern user has requested */
    private Pattern subscribedPattern;

    /* the list of topics the user has requested */
    private final Set<String> subscription;

    /* the list of topics the group has subscribed to (set only for the leader on join group completion) */
    private final Set<String> groupSubscription;

    /* the list of partitions the user has requested */
    private final Set<TopicPartition> userAssignment;

    /* the list of partitions currently assigned */
    private final Map<TopicPartition, TopicPartitionState> assignment;

    /**
     * 我们需要从协调器请求一个分区分配吗?
     */
    private boolean needsPartitionAssignment;

    /* do we need to request the latest committed offsets from the coordinator? */
    private boolean needsFetchCommittedOffsets;

    /* Default offset reset strategy */
    private final OffsetResetStrategy defaultResetStrategy;

    /* Listener to be invoked when assignment changes */
    private ConsumerRebalanceListener listener;

    private static final String SUBSCRIPTION_EXCEPTION_MESSAGE = "Subscription to topics, partitions and pattern are mutually exclusive";

    public SubscriptionState(OffsetResetStrategy defaultResetStrategy) {
        this.defaultResetStrategy = defaultResetStrategy;
        this.subscription = new HashSet<>();
        this.userAssignment = new HashSet<>();
        this.assignment = new HashMap<>();
        this.groupSubscription = new HashSet<>();
        this.needsPartitionAssignment = false;
        this.needsFetchCommittedOffsets = true; // initialize to true for the consumers to fetch offset upon starting up
        this.subscribedPattern = null;
    }

    public void subscribe(List<String> topics, ConsumerRebalanceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RebalanceListener cannot be null");
        }
        if (!this.userAssignment.isEmpty() || this.subscribedPattern != null) {
            // 指定主题，自动分配分区，与其他两种互斥
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);
        }
        this.listener = listener;
        changeSubscription(topics);
    }

    public void changeSubscription(List<String> topicsToSubscribe) {
        if (!this.subscription.equals(new HashSet<>(topicsToSubscribe))) {
            this.subscription.clear();
            this.subscription.addAll(topicsToSubscribe);
            this.groupSubscription.addAll(topicsToSubscribe);
            this.needsPartitionAssignment = true;
            // 删除任何已分配的不再订阅的分区
            for (Iterator<TopicPartition> it = assignment.keySet().iterator(); it.hasNext(); ) {
                TopicPartition tp = it.next();
                if (!subscription.contains(tp.topic())) {
                    it.remove();
                }
            }
        }
    }

    /**
     * 向当前组订阅添加主题。
     * 组长使用它来确保它接收到组感兴趣的所有主题的元数据更新。
     * @param topics 要添加到组订阅的主题
     */
    public void groupSubscribe(Collection<String> topics) {
        if (!this.userAssignment.isEmpty()) {
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);
        }
        this.groupSubscription.addAll(topics);
    }

    public void needReassignment() {
        this.groupSubscription.retainAll(subscription);
        this.needsPartitionAssignment = true;
    }

    /**
     * 将分配更改为用户提供的指定分区，注意这与{@link #assignFromSubscribed(Collection)}不同，后者的输入分区来自订阅的主题。
     */
    public void assignFromUser(Collection<TopicPartition> partitions) {
        if (!this.subscription.isEmpty() || this.subscribedPattern != null) {
            // 用户指定分区，与其他两种互斥
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);
        }

        this.userAssignment.clear();
        this.userAssignment.addAll(partitions);

        for (TopicPartition partition : partitions) {
            if (!assignment.containsKey(partition)) {
                addAssignedPartition(partition);
            }
        }

        this.assignment.keySet().retainAll(this.userAssignment);

        this.needsPartitionAssignment = false;
    }

    /**
     * Change the assignment to the specified partitions returned from the coordinator,
     * note this is different from {@link #assignFromUser(Collection)} which directly set the assignment from user inputs
     */
    public void assignFromSubscribed(Collection<TopicPartition> assignments) {
        for (TopicPartition tp : assignments) {
            if (!this.subscription.contains(tp.topic())) {
                throw new IllegalArgumentException("Assigned partition " + tp + " for non-subscribed topic.");
            }
        }
        this.assignment.clear();
        for (TopicPartition tp : assignments) {
            addAssignedPartition(tp);
        }
        this.needsPartitionAssignment = false;
    }

    public void subscribe(Pattern pattern, ConsumerRebalanceListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("RebalanceListener cannot be null");
        }

        if (!this.subscription.isEmpty() || !this.userAssignment.isEmpty()) {
            // 模式匹配，与其他两种互斥
            throw new IllegalStateException(SUBSCRIPTION_EXCEPTION_MESSAGE);
        }

        this.listener = listener;
        this.subscribedPattern = pattern;
    }

    public boolean hasPatternSubscription() {
        return subscribedPattern != null;
    }

    public void unsubscribe() {
        this.subscription.clear();
        this.userAssignment.clear();
        this.assignment.clear();
        this.needsPartitionAssignment = true;
        this.subscribedPattern = null;
    }

    public Pattern getSubscribedPattern() {
        return this.subscribedPattern;
    }

    public Set<String> subscription() {
        return this.subscription;
    }

    /**
     * Get the subscription for the group. For the leader, this will include the union of the
     * subscriptions of all group members. For followers, it is just that member's subscription.
     * This is used when querying topic metadata to detect the metadata changes which would
     * require rebalancing. The leader fetches metadata for all topics in the group so that it
     * can do the partition assignment (which requires at least partition counts for all topics
     * to be assigned).
     * @return The union of all subscribed topics in the group if this member is the leader
     * of the current generation; otherwise it returns the same set as {@link #subscription()}
     */
    public Set<String> groupSubscription() {
        return this.groupSubscription;
    }

    private TopicPartitionState assignedState(TopicPartition tp) {
        TopicPartitionState state = this.assignment.get(tp);
        if (state == null) {
            throw new IllegalStateException("No current assignment for partition " + tp);
        }
        return state;
    }

    public void committed(TopicPartition tp, OffsetAndMetadata offset) {
        assignedState(tp).committed(offset);
    }

    public OffsetAndMetadata committed(TopicPartition tp) {
        return assignedState(tp).committed;
    }

    public void needRefreshCommits() {
        this.needsFetchCommittedOffsets = true;
    }

    public boolean refreshCommitsNeeded() {
        return this.needsFetchCommittedOffsets;
    }

    public void commitsRefreshed() {
        this.needsFetchCommittedOffsets = false;
    }

    public void seek(TopicPartition tp, long offset) {
        assignedState(tp).seek(offset);
    }

    public Set<TopicPartition> assignedPartitions() {
        return this.assignment.keySet();
    }

    public Set<TopicPartition> fetchablePartitions() {
        Set<TopicPartition> fetchable = new HashSet<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            if (entry.getValue().isFetchable()) {
                fetchable.add(entry.getKey());
            }
        }
        return fetchable;
    }

    public boolean partitionsAutoAssigned() {
        return !this.subscription.isEmpty();
    }

    public void position(TopicPartition tp, long offset) {
        assignedState(tp).position(offset);
    }

    public Long position(TopicPartition tp) {
        return assignedState(tp).position;
    }

    public Map<TopicPartition, OffsetAndMetadata> allConsumed() {
        Map<TopicPartition, OffsetAndMetadata> allConsumed = new HashMap<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            TopicPartitionState state = entry.getValue();
            if (state.hasValidPosition) {
                allConsumed.put(entry.getKey(), new OffsetAndMetadata(state.position));
            }
        }
        return allConsumed;
    }

    public void needOffsetReset(TopicPartition partition, OffsetResetStrategy offsetResetStrategy) {
        assignedState(partition).awaitReset(offsetResetStrategy);
    }

    public void needOffsetReset(TopicPartition partition) {
        needOffsetReset(partition, defaultResetStrategy);
    }

    public boolean hasDefaultOffsetResetPolicy() {
        return defaultResetStrategy != OffsetResetStrategy.NONE;
    }

    public boolean isOffsetResetNeeded(TopicPartition partition) {
        return assignedState(partition).awaitingReset;
    }

    public OffsetResetStrategy resetStrategy(TopicPartition partition) {
        return assignedState(partition).resetStrategy;
    }

    public boolean hasAllFetchPositions() {
        for (TopicPartitionState state : assignment.values()) {
            if (!state.hasValidPosition) {
                return false;
            }
        }
        return true;
    }

    public Set<TopicPartition> missingFetchPositions() {
        Set<TopicPartition> missing = new HashSet<>();
        for (Map.Entry<TopicPartition, TopicPartitionState> entry : assignment.entrySet()) {
            if (!entry.getValue().hasValidPosition) {
                missing.add(entry.getKey());
            }
        }
        return missing;
    }

    public boolean partitionAssignmentNeeded() {
        return this.needsPartitionAssignment;
    }

    public boolean isAssigned(TopicPartition tp) {
        return assignment.containsKey(tp);
    }

    public boolean isPaused(TopicPartition tp) {
        return isAssigned(tp) && assignedState(tp).paused;
    }

    public boolean isFetchable(TopicPartition tp) {
        return isAssigned(tp) && assignedState(tp).isFetchable();
    }

    public void pause(TopicPartition tp) {
        assignedState(tp).pause();
    }

    public void resume(TopicPartition tp) {
        assignedState(tp).resume();
    }

    private void addAssignedPartition(TopicPartition tp) {
        this.assignment.put(tp, new TopicPartitionState());
    }

    public ConsumerRebalanceListener listener() {
        return listener;
    }

    private static class TopicPartitionState {
        private Long position;
        private OffsetAndMetadata committed;  // last committed position

        private boolean hasValidPosition; // whether we have valid consumed and fetched positions
        private boolean paused;  // whether this partition has been paused by the user
        private boolean awaitingReset; // whether we are awaiting reset
        private OffsetResetStrategy resetStrategy;  // the reset strategy if awaitingReset is set

        public TopicPartitionState() {
            this.paused = false;
            this.position = null;
            this.committed = null;
            this.awaitingReset = false;
            this.hasValidPosition = false;
            this.resetStrategy = null;
        }

        private void awaitReset(OffsetResetStrategy strategy) {
            this.awaitingReset = true;
            this.resetStrategy = strategy;
            this.position = null;
            this.hasValidPosition = false;
        }

        private void seek(long offset) {
            this.position = offset;
            this.awaitingReset = false;
            this.resetStrategy = null;
            this.hasValidPosition = true;
        }

        private void position(long offset) {
            if (!hasValidPosition) {
                throw new IllegalStateException("Cannot update fetch position without valid consumed/fetched positions");
            }
            this.position = offset;
        }

        private void committed(OffsetAndMetadata offset) {
            this.committed = offset;
        }

        private void pause() {
            this.paused = true;
        }

        private void resume() {
            this.paused = false;
        }

        private boolean isFetchable() {
            return !paused && hasValidPosition;
        }

    }

}