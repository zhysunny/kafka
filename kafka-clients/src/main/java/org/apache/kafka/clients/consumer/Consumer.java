/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE
 * file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file
 * to You under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.annotation.InterfaceStability;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @see KafkaConsumer
 * @see MockConsumer
 */
@InterfaceStability.Unstable
public interface Consumer<K, V> extends Closeable {
    
    /**
     * @see KafkaConsumer#assignment()
     */
    public Set<TopicPartition> assignment();

    /**
     * @see KafkaConsumer#subscription()
     */
    public Set<String> subscription();

    /**
     * @see KafkaConsumer#subscribe(List)
     */
    public void subscribe(List<String> topics);

    /**
     * @see KafkaConsumer#subscribe(List, ConsumerRebalanceListener)
     */
    public void subscribe(List<String> topics, ConsumerRebalanceListener callback);

    /**
     * @see KafkaConsumer#assign(List)
     */
    public void assign(List<TopicPartition> partitions);

    /**
    * @see KafkaConsumer#subscribe(Pattern, ConsumerRebalanceListener)
    */
    public void subscribe(Pattern pattern, ConsumerRebalanceListener callback);

    /**
     * @see KafkaConsumer#unsubscribe()
     */
    public void unsubscribe();

    /**
     * @see KafkaConsumer#poll(long)
     */
    public ConsumerRecords<K, V> poll(long timeout);

    /**
     * @see KafkaConsumer#commitSync()
     */
    public void commitSync();

    /**
     * @see KafkaConsumer#commitSync(Map)
     */
    public void commitSync(Map<TopicPartition, OffsetAndMetadata> offsets);

    /**
     * @see KafkaConsumer#commitAsync()
     */
    public void commitAsync();

    /**
     * @see KafkaConsumer#commitAsync(OffsetCommitCallback)
     */
    public void commitAsync(OffsetCommitCallback callback);

    /**
     * @see KafkaConsumer#commitAsync(Map, OffsetCommitCallback)
     */
    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> offsets, OffsetCommitCallback callback);

    /**
     * @see KafkaConsumer#seek(TopicPartition, long)
     */
    public void seek(TopicPartition partition, long offset);

    /**
     * @see KafkaConsumer#seekToBeginning(TopicPartition...)
     */
    public void seekToBeginning(TopicPartition... partitions);

    /**
     * @see KafkaConsumer#seekToEnd(TopicPartition...)
     */
    public void seekToEnd(TopicPartition... partitions);

    /**
     * @see KafkaConsumer#position(TopicPartition)
     */
    public long position(TopicPartition partition);

    /**
     * @see KafkaConsumer#committed(TopicPartition)
     */
    public OffsetAndMetadata committed(TopicPartition partition);

    /**
     * @see KafkaConsumer#metrics()
     */
    public Map<MetricName, ? extends Metric> metrics();

    /**
     * @see KafkaConsumer#partitionsFor(String)
     */
    public List<PartitionInfo> partitionsFor(String topic);

    /**
     * @see KafkaConsumer#listTopics()
     */
    public Map<String, List<PartitionInfo>> listTopics();

    /**
     * @see KafkaConsumer#pause(TopicPartition...)
     */
    public void pause(TopicPartition... partitions);

    /**
     * @see KafkaConsumer#resume(TopicPartition...)
     */
    public void resume(TopicPartition... partitions);

    /**
     * @see KafkaConsumer#close()
     */
    public void close();

    /**
     * @see KafkaConsumer#wakeup()
     */
    public void wakeup();

}
