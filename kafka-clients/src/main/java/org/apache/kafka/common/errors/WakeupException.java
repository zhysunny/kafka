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
package org.apache.kafka.common.errors;

import org.apache.kafka.common.KafkaException;

/**
 * 异常，用于指示外部线程抢占阻塞操作。
 * 例如，{@link org.apache.kafka.clients.consumer。可以使用KafkaConsumer#wakeup}从活动的{@link org.apache.kafka.clients.consumer.KafkaConsumer#poll(long)}中跳出，该活动将引发此异常的实例。
 */
public class WakeupException extends KafkaException {
    private static final long serialVersionUID = 1L;

}
