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
package org.apache.kafka.clients.consumer;

import org.apache.kafka.common.KafkaException;

/**
 * 当带有{@link KafkaConsumer#commitSync()}的偏移提交失败并出现不可恢复的错误时，将引发此异常。
 * 在成功应用提交之前，组平衡完成时可能会发生这种情况。
 * 在这种情况下，提交通常不能重试，因为有些分区可能已经分配给组中的另一个成员了。
 * @author 章云
 * @date 2020/2/27 8:47
 */
public class CommitFailedException extends KafkaException {

    private static final long serialVersionUID = 1L;

    public CommitFailedException(String message) {
        super(message);
    }

}
