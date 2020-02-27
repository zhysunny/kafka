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

/**
 * 对于给定的分区，当前没有可用的leader(可能是因为正在进行领导选举，也可能是因为所有副本都已关闭)。
 */
public class LeaderNotAvailableException extends InvalidMetadataException {

    private static final long serialVersionUID = 1L;

    public LeaderNotAvailableException(String message) {
        super(message);
    }

}
