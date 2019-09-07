/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kafka.admin

import junit.framework.Assert._
import org.junit.Test
import kafka.utils.Logging
import kafka.utils.TestUtils
import kafka.zk.ZooKeeperTestHarness
import kafka.server.ConfigType
import kafka.admin.TopicCommand.TopicCommandOptions
import kafka.utils.ZkUtils._
import kafka.coordinator.GroupCoordinator

class TopicCommandTest extends ZooKeeperTestHarness with Logging {

  @Test
  def testConfigPreservationAcrossPartitionAlteration() {
    val topic = "test"
    val numPartitionsOriginal = 1
    val cleanupKey = "cleanup.policy"
    val cleanupVal = "compact"
    // create brokers
    val brokers = List(0, 1, 2)
    TestUtils.createBrokersInZk(zkUtils, brokers)
    // create the topic
    val createOpts = new TopicCommandOptions(Array("--partitions", numPartitionsOriginal.toString,
      "--replication-factor", "1",
      "--config", cleanupKey + "=" + cleanupVal,
      "--topic", topic))
    TopicCommand.createTopic(zkUtils, createOpts)
    val props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic, topic)
    assertTrue("Properties after creation don't contain " + cleanupKey, props.containsKey(cleanupKey))
    assertTrue("Properties after creation have incorrect value", props.getProperty(cleanupKey).equals(cleanupVal))

    // pre-create the topic config changes path to avoid a NoNodeException
    zkUtils.createPersistentPath(EntityConfigChangesPath)

    // modify the topic to add new partitions
    val numPartitionsModified = 3
    val alterOpts = new TopicCommandOptions(Array("--partitions", numPartitionsModified.toString, "--topic", topic))
    TopicCommand.alterTopic(zkUtils, alterOpts)
    val newProps = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic, topic)
    assertTrue("Updated properties do not contain " + cleanupKey, newProps.containsKey(cleanupKey))
    assertTrue("Updated properties have incorrect value", newProps.getProperty(cleanupKey).equals(cleanupVal))
  }

  @Test
  def testTopicDeletion() {
    val normalTopic = "test"

    val numPartitionsOriginal = 1

    // create brokers
    val brokers = List(0, 1, 2)
    TestUtils.createBrokersInZk(zkUtils, brokers)

    // create the NormalTopic
    val createOpts = new TopicCommandOptions(Array("--partitions", numPartitionsOriginal.toString,
      "--replication-factor", "1",
      "--topic", normalTopic))
    TopicCommand.createTopic(zkUtils, createOpts)

    // delete the NormalTopic
    val deleteOpts = new TopicCommandOptions(Array("--topic", normalTopic))
    val deletePath = getDeleteTopicPath(normalTopic)
    assertFalse("Delete path for topic shouldn't exist before deletion.", zkUtils.zkClient.exists(deletePath))
    TopicCommand.deleteTopic(zkUtils, deleteOpts)
    assertTrue("Delete path for topic should exist after deletion.", zkUtils.zkClient.exists(deletePath))

    // create the offset topic
    val createOffsetTopicOpts = new TopicCommandOptions(Array("--partitions", numPartitionsOriginal.toString,
      "--replication-factor", "1",
      "--topic", GroupCoordinator.GroupMetadataTopicName))
    TopicCommand.createTopic(zkUtils, createOffsetTopicOpts)

    // try to delete the GroupCoordinator.GroupMetadataTopicName and make sure it doesn't
    val deleteOffsetTopicOpts = new TopicCommandOptions(Array("--topic", GroupCoordinator.GroupMetadataTopicName))
    val deleteOffsetTopicPath = getDeleteTopicPath(GroupCoordinator.GroupMetadataTopicName)
    assertFalse("Delete path for topic shouldn't exist before deletion.", zkUtils.zkClient.exists(deleteOffsetTopicPath))
    intercept[AdminOperationException] {
        TopicCommand.deleteTopic(zkUtils, deleteOffsetTopicOpts)
    }
    assertFalse("Delete path for topic shouldn't exist after deletion.", zkUtils.zkClient.exists(deleteOffsetTopicPath))
  }
}
