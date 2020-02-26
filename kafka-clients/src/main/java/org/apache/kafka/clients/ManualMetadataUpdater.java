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

package org.apache.kafka.clients;

import org.apache.kafka.common.Node;
import org.apache.kafka.common.protocol.types.Struct;

import java.util.ArrayList;
import java.util.List;

/**
 * ' MetadataUpdater '的一个简单实现，它通过构造函数或' setNodes '返回集群节点集。
 * 这在不需要自动元数据更新的情况下非常有用。控制器/代理通信就是一个例子。
 * 这个类不是线程安全的!
 */
public class ManualMetadataUpdater implements MetadataUpdater {

    private List<Node> nodes;

    public ManualMetadataUpdater() {
        this(new ArrayList<Node>(0));
    }

    public ManualMetadataUpdater(List<Node> nodes) {
        this.nodes = nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public List<Node> fetchNodes() {
        return new ArrayList<>(nodes);
    }

    @Override
    public boolean isUpdateDue(long now) {
        return false;
    }

    @Override
    public long maybeUpdate(long now) {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean maybeHandleDisconnection(ClientRequest request) {
        return false;
    }

    @Override
    public boolean maybeHandleCompletedReceive(ClientRequest request, long now, Struct body) {
        return false;
    }

    @Override
    public void requestUpdate() {
        // Do nothing
    }
}
