/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
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
 */

public interface Partitioner extends Configurable {

    /**
     * Compute the partition for the given record.
     *
     * @param topic The topic name
     * @param keyBytes The serialized key to partition on( or null if no key)
     * @param cluster The current cluster metadata
     */
    public int partition(String topic, byte[] keyBytes, Cluster cluster);

    /**
     * This is called when partitioner is closed.
     */
    public void close();

}
