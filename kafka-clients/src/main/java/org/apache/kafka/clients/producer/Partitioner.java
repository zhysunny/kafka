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

package org.apache.kafka.clients.producer;

import org.apache.kafka.common.Configurable;
import org.apache.kafka.common.Cluster;

/**
 * Partitioner Interface
 * @author 章云
 * @date 2020/1/16 19:17
 */
public interface Partitioner extends Configurable {

    /**
     * 计算给定记录的分区。
     * @param topic    The topic name
     * @param keyBytes The serialized key to partition on( or null if no key)
     * @param cluster  The current cluster metadata
     * @return
     */
    int partition(String topic, byte[] keyBytes, Cluster cluster);

    /**
     * 这在分区程序关闭时调用。
     */
    void close();

}
