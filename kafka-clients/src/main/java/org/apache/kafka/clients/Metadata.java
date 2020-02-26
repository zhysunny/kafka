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
package org.apache.kafka.clients;

import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;

/**
 * 封装了一些元数据逻辑的类。
 * 这个类由客户端线程(用于分区)和后台发送方线程共享。
 * 元数据只维护主题的一个子集，可以随着时间的推移添加到其中。
 * 当我们为一个主题请求元数据时，我们没有任何元数据，这将触发元数据更新。
 * @author 章云
 * @date 2020/1/23 11:06
 */
public final class Metadata {

    private static final Logger log = LoggerFactory.getLogger(Metadata.class);

    /**
     * 更新失败的情况下，下1次更新的补偿时间（这个变量在代码中意义不是太大）
     */
    private final long refreshBackoffMs;
    /**
     * 关键值：每隔多久，更新一次。缺省是5 * 60 * 1000，也就是5分种
     */
    private final long metadataExpireMs;
    /**
     * 每更新成功1次，version递增1。这个变量主要用于在while循环，wait的时候，作为循环判断条件
     */
    private int version;
    /**
     * 上一次更新时间（也包含更新失败的情况）
     */
    private long lastRefreshMs;
    /**
     * 上一次成功更新的时间（如果每次都成功的话，则2者相等。否则，lastSuccessulRefreshMs < lastRefreshMs)
     */
    private long lastSuccessfulRefreshMs;
    /**
     * 集群配置信息
     */
    private Cluster cluster;
    /**
     * 是否强制刷新
     */
    private boolean needUpdate;

    private final Set<String> topics;
    private final List<Listener> listeners;
    private boolean needMetadataForAllTopics;

    /**
     * Create a metadata instance with reasonable defaults
     */
    public Metadata() {
        this(100L, 60 * 60 * 1000L);
    }

    /**
     * Create a new Metadata instance
     * @param refreshBackoffMs 刷新元数据之间必须过期的最短时间，以避免繁忙的轮询
     * @param metadataExpireMs 无需刷新即可保留元数据的最大时间量
     */
    public Metadata(long refreshBackoffMs, long metadataExpireMs) {
        this.refreshBackoffMs = refreshBackoffMs;
        this.metadataExpireMs = metadataExpireMs;
        this.lastRefreshMs = 0L;
        this.lastSuccessfulRefreshMs = 0L;
        this.version = 0;
        this.cluster = Cluster.empty();
        this.needUpdate = false;
        this.topics = new HashSet<String>();
        this.listeners = new ArrayList<>();
        this.needMetadataForAllTopics = false;
    }

    /**
     * 获取当前集群信息而不阻塞
     */
    public synchronized Cluster fetch() {
        return this.cluster;
    }

    /**
     * Add the topic to maintain in the metadata
     */
    public synchronized void add(String topic) {
        topics.add(topic);
    }

    /**
     * 下一次更新集群信息的时间是当前信息过期和当前信息可以更新的时间的最大值(即已经过了回退时间);
     * 如果已请求更新，则过期时间为现在
     */
    public synchronized long timeToNextUpdate(long nowMs) {
        long timeToExpire = needUpdate ? 0 : Math.max(this.lastSuccessfulRefreshMs + this.metadataExpireMs - nowMs, 0);
        long timeToAllowUpdate = this.lastRefreshMs + this.refreshBackoffMs - nowMs;
        return Math.max(timeToExpire, timeToAllowUpdate);
    }

    /**
     * Request an update of the current cluster metadata info, return the current version before the update
     */
    public synchronized int requestUpdate() {
        this.needUpdate = true;
        return this.version;
    }

    /**
     * 等待元数据更新，直到当前版本大于我们所知道的最新版本
     */
    public synchronized void awaitUpdate(final int lastVersion, final long maxWaitMs) throws InterruptedException {
        if (maxWaitMs < 0) {
            throw new IllegalArgumentException("Max time to wait for metadata updates should not be < 0 milli seconds");
        }
        long begin = System.currentTimeMillis();
        long remainingWaitMs = maxWaitMs;
        while (this.version <= lastVersion) {
            //当Sender成功更新meatadata之后，version加1。否则会循环，一直wait
            if (remainingWaitMs != 0) {
                //线程的wait机制，wait和synchronized的配合使用
                //notify在Sender更新Metadata的时候发出。
                wait(remainingWaitMs);
            }
            long elapsed = System.currentTimeMillis() - begin;
            if (elapsed >= maxWaitMs) {
                //wait时间超出了最长等待时间
                throw new TimeoutException("Failed to update metadata after " + maxWaitMs + " ms.");
            }
            remainingWaitMs = maxWaitMs - elapsed;
        }
    }

    /**
     * Replace the current set of topics maintained to the one provided
     * @param topics
     */
    public synchronized void setTopics(Collection<String> topics) {
        if (!this.topics.containsAll(topics)) {
            requestUpdate();
        }
        this.topics.clear();
        this.topics.addAll(topics);
    }

    /**
     * Get the list of topics we are currently maintaining metadata for
     */
    public synchronized Set<String> topics() {
        return new HashSet<String>(this.topics);
    }

    /**
     * Check if a topic is already in the topic set.
     * @param topic topic to check
     * @return true if the topic exists, false otherwise
     */
    public synchronized boolean containsTopic(String topic) {
        return this.topics.contains(topic);
    }

    /**
     * 更新成功，version+1, 同时更新其它字段
     */
    public synchronized void update(Cluster cluster, long now) {
        this.needUpdate = false;
        this.lastRefreshMs = now;
        this.lastSuccessfulRefreshMs = now;
        this.version += 1;

        for (Listener listener : listeners) {
            //如果有人监听了metadata的更新，通知他们
            listener.onMetadataUpdate(cluster);
        }

        // Do this after notifying listeners as subscribed topics' list can be changed by listeners
        this.cluster = this.needMetadataForAllTopics ? getClusterForCurrentTopics(cluster) : cluster;
        //通知所有的阻塞的producer线程
        notifyAll();
        log.debug("Updated cluster metadata version {} to {}", this.version, this.cluster);
    }

    /**
     * 更新失败，只更新lastRefreshMs
     * Record an attempt to update the metadata that failed. We need to keep track of this
     * to avoid retrying immediately.
     */
    public synchronized void failedUpdate(long now) {
        this.lastRefreshMs = now;
    }

    /**
     * @return The current metadata version
     */
    public synchronized int version() {
        return this.version;
    }

    /**
     * The last time metadata was successfully updated.
     */
    public synchronized long lastSuccessfulUpdate() {
        return this.lastSuccessfulRefreshMs;
    }

    /**
     * The metadata refresh backoff in ms
     */
    public long refreshBackoff() {
        return refreshBackoffMs;
    }

    /**
     * Set state to indicate if metadata for all topics in Kafka cluster is required or not.
     * @param needMetadaForAllTopics boolean indicating need for metadata of all topics in cluster.
     */
    public synchronized void needMetadataForAllTopics(boolean needMetadaForAllTopics) {
        this.needMetadataForAllTopics = needMetadaForAllTopics;
    }

    /**
     * Get whether metadata for all topics is needed or not
     */
    public synchronized boolean needMetadataForAllTopics() {
        return this.needMetadataForAllTopics;
    }

    /**
     * Add a Metadata listener that gets notified of metadata updates
     */
    public synchronized void addListener(Listener listener) {
        this.listeners.add(listener);
    }

    /**
     * Stop notifying the listener of metadata updates
     */
    public synchronized void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    /**
     * MetadataUpdate Listener
     */
    public interface Listener {

        void onMetadataUpdate(Cluster cluster);

    }

    private Cluster getClusterForCurrentTopics(Cluster cluster) {
        Set<String> unauthorizedTopics = new HashSet<>();
        Collection<PartitionInfo> partitionInfos = new ArrayList<>();
        List<Node> nodes = Collections.emptyList();
        if (cluster != null) {
            unauthorizedTopics.addAll(cluster.unauthorizedTopics());
            unauthorizedTopics.retainAll(this.topics);

            for (String topic : this.topics) {
                partitionInfos.addAll(cluster.partitionsForTopic(topic));
            }
            nodes = cluster.nodes();
        }
        return new Cluster(nodes, partitionInfos, unauthorizedTopics);
    }

}
