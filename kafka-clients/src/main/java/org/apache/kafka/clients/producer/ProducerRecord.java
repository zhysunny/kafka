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
package org.apache.kafka.clients.producer;

/**
 * Kafka生产者消息封装
 * 要发送到Kafka的键/值对。这包括发送记录的主题名称、可选的分区号和可选的键和值。
 * 如果指定了有效的分区号，则在发送记录时将使用该分区。
 * 如果没有指定分区，但是存在一个键，那么将使用该键的散列选择一个分区。
 * 如果既不存在键也不存在分区，那么将以循环方式分配分区。
 * @author 章云
 * @date 2020/1/16 9:06
 */
public final class ProducerRecord<K, V> {

    private final String topic;
    private final Integer partition;
    private final K key;
    private final V value;

    /**
     * Creates a record to be sent to a specified topic and partition
     * @param topic     The topic the record will be appended to
     * @param partition The partition to which the record should be sent
     * @param key       The key that will be included in the record
     * @param value     The record contents
     */
    public ProducerRecord(String topic, Integer partition, K key, V value) {
        if (topic == null) {
            throw new IllegalArgumentException("Topic cannot be null");
        }
        this.topic = topic;
        this.partition = partition;
        this.key = key;
        this.value = value;
    }

    /**
     * Create a record to be sent to Kafka
     * @param topic The topic the record will be appended to
     * @param key   The key that will be included in the record
     * @param value The record contents
     */
    public ProducerRecord(String topic, K key, V value) {
        this(topic, null, key, value);
    }

    /**
     * Create a record with no key
     * @param topic The topic this record should be sent to
     * @param value The record contents
     */
    public ProducerRecord(String topic, V value) {
        this(topic, null, value);
    }

    /**
     * The topic this record is being sent to
     */
    public String topic() {
        return topic;
    }

    /**
     * The key (or null if no key is specified)
     */
    public K key() {
        return key;
    }

    /**
     * @return The value
     */
    public V value() {
        return value;
    }

    /**
     * The partition to which the record will be sent (or null if no partition was specified)
     */
    public Integer partition() {
        return partition;
    }

    @Override
    public String toString() {
        String key = this.key == null ? "null" : this.key.toString();
        String value = this.value == null ? "null" : this.value.toString();
        return "ProducerRecord(topic=" + topic + ", partition=" + partition + ", key=" + key + ", value=" + value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof ProducerRecord)) {
            return false;
        }

        ProducerRecord<?, ?> that = (ProducerRecord<?, ?>)o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        } else if (partition != null ? !partition.equals(that.partition) : that.partition != null) {
            return false;
        } else if (topic != null ? !topic.equals(that.topic) : that.topic != null) {
            return false;
        } else if (value != null ? !value.equals(that.value) : that.value != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = topic != null ? topic.hashCode() : 0;
        result = 31 * result + (partition != null ? partition.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

}
