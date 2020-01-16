/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.clients.producer.internals;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.utils.Utils;

/**
 * 默认分区策略
 * <ul>
 * <li>如果在记录中指定了分区，那么就使用它
 * <li>如果没有指定分区，但存在一个键，则根据该键的散列选择一个分区
 * <li>如果没有分区或键，则以循环方式选择分区
 * @author 章云
 * @date 2020/1/16 19:14
 */
public class DefaultPartitioner implements Partitioner {

    private final AtomicInteger counter = new AtomicInteger(new Random().nextInt());

    /**
     * 一种确定地将数字转换为正数的廉价方法。
     * 当输入为正数时，返回原始值。
     * 当输入数字为负数时，返回的正数是原始值位，而0x7fffffff不是它的绝对值。
     * 注意:将来更改此方法可能导致分区选择与已放在分区上的现有消息不兼容。
     * @param number 一个给定的数
     * @return 一个正数
     */
    private static int toPositive(int number) {
        return number & 0x7fffffff;
    }

    @Override
    public void configure(Map<String, ?> configs) {}

    /**
     * 计算给定记录的分区。
     * @param topic    The topic name
     * @param keyBytes serialized key to partition on (or null if no key)
     * @param cluster  The current cluster metadata
     */
    @Override
    public int partition(String topic, byte[] keyBytes, Cluster cluster) {
        List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        int numPartitions = partitions.size();
        if (keyBytes == null) {
            // 当key设置为null，获取一个随机序列值
            int nextValue = counter.getAndIncrement();
            List<PartitionInfo> availablePartitions = cluster.availablePartitionsForTopic(topic);
            if (availablePartitions.size() > 0) {
                int part = DefaultPartitioner.toPositive(nextValue) % availablePartitions.size();
                return availablePartitions.get(part).partition();
            } else {
                // no partitions are available, give a non-available partition
                return DefaultPartitioner.toPositive(nextValue) % numPartitions;
            }
        } else {
            // hash the keyBytes to choose a partition
            return DefaultPartitioner.toPositive(Utils.murmur2(keyBytes)) % numPartitions;
        }
    }

    @Override
    public void close() {}

}
