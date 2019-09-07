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

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.test.MockMetricsReporter;
import org.apache.kafka.test.MockSerializer;
import org.junit.Assert;
import org.junit.Test;

import java.util.Properties;
import java.util.Map;
import java.util.HashMap;

public class KafkaProducerTest {

    @Test
    public void testConstructorFailureCloseResource() {
        Properties props = new Properties();
        props.setProperty(ProducerConfig.CLIENT_ID_CONFIG, "testConstructorClose");
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "some.invalid.hostname.foo.bar:9999");
        props.setProperty(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, MockMetricsReporter.class.getName());

        final int oldInitCount = MockMetricsReporter.INIT_COUNT.get();
        final int oldCloseCount = MockMetricsReporter.CLOSE_COUNT.get();
        try {
            KafkaProducer<byte[], byte[]> producer = new KafkaProducer<byte[], byte[]>(
                    props, new ByteArraySerializer(), new ByteArraySerializer());
        } catch (KafkaException e) {
            Assert.assertEquals(oldInitCount + 1, MockMetricsReporter.INIT_COUNT.get());
            Assert.assertEquals(oldCloseCount + 1, MockMetricsReporter.CLOSE_COUNT.get());
            Assert.assertEquals("Failed to construct kafka producer", e.getMessage());
            return;
        }
        Assert.fail("should have caught an exception and returned");
    }

    @Test
    public void testSerializerClose() throws Exception {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.CLIENT_ID_CONFIG, "testConstructorClose");
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9999");
        configs.put(ProducerConfig.METRIC_REPORTER_CLASSES_CONFIG, MockMetricsReporter.class.getName());
        configs.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL);
        final int oldInitCount = MockSerializer.INIT_COUNT.get();
        final int oldCloseCount = MockSerializer.CLOSE_COUNT.get();

        KafkaProducer<byte[], byte[]> producer = new KafkaProducer<byte[], byte[]>(
                configs, new MockSerializer(), new MockSerializer());
        Assert.assertEquals(oldInitCount + 2, MockSerializer.INIT_COUNT.get());
        Assert.assertEquals(oldCloseCount, MockSerializer.CLOSE_COUNT.get());

        producer.close();
        Assert.assertEquals(oldInitCount + 2, MockSerializer.INIT_COUNT.get());
        Assert.assertEquals(oldCloseCount + 2, MockSerializer.CLOSE_COUNT.get());
    }
}
