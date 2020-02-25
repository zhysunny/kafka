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
package org.apache.kafka.clients;

import java.util.HashMap;
import java.util.Map;

/**
 * 与集群中每个节点的连接状态。
 * @author 章云
 * @date 2020/2/25 16:40
 */
final class ClusterConnectionStates {

    /**
     * reconnect.backoff.ms  default=50  重新连接的间隔时间
     */
    private final long reconnectBackoffMs;
    /**
     * 每个节点的连接状态
     */
    private final Map<String, NodeConnectionState> nodeState;

    public ClusterConnectionStates(long reconnectBackoffMs) {
        this.reconnectBackoffMs = reconnectBackoffMs;
        this.nodeState = new HashMap<>();
    }

    /**
     * 如果我们现在可以开始一个新的连接。
     * 如果我们没有连接并且至少在最小的重新连接回退期间没有连接，则会出现这种情况。
     * @param id  The connection id to check
     * @param now The current time in MS
     * @return true if we can initiate a new connection
     */
    public boolean canConnect(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null) {
            return true;
        } else {
            return state.state == ConnectionState.DISCONNECTED && now - state.lastConnectAttemptMs >= this.reconnectBackoffMs;
        }
    }

    /**
     * 如果我们从给定节点断开连接，并且还不能重新建立连接，则返回true
     * @param id  The connection to check
     * @param now The current time in ms
     */
    public boolean isBlackedOut(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null) {
            return false;
        } else {
            return state.state == ConnectionState.DISCONNECTED && now - state.lastConnectAttemptMs < this.reconnectBackoffMs;
        }
    }

    /**
     * 根据连接状态返回尝试发送数据之前等待的毫秒数。
     * 断开连接时，这将考虑重新连接的回退时间。
     * 当连接或连接时，它处理慢速/停顿的连接。
     * @param id  The connection to check
     * @param now The current time in ms
     */
    public long connectionDelay(String id, long now) {
        NodeConnectionState state = nodeState.get(id);
        if (state == null) {
            return 0;
        }
        long timeWaited = now - state.lastConnectAttemptMs;
        if (state.state == ConnectionState.DISCONNECTED) {
            return Math.max(this.reconnectBackoffMs - timeWaited, 0);
        } else {
            // 当连接或连接时，我们应该能够无限期地延迟，因为一旦数据可以发送，其他事件(连接或数据被攻击)将导致唤醒。
            return Long.MAX_VALUE;
        }
    }

    /**
     * 输入正在连接中
     * @param id  The id of the connection
     * @param now The current time.
     */
    public void connecting(String id, long now) {
        nodeState.put(id, new NodeConnectionState(ConnectionState.CONNECTING, now));
    }

    /**
     * 已连接成功返回true
     * @param id The id of the connection to check
     */
    public boolean isConnected(String id) {
        NodeConnectionState state = nodeState.get(id);
        return state != null && state.state == ConnectionState.CONNECTED;
    }

    /**
     * 正在连接返回true
     * @param id The id of the connection
     */
    public boolean isConnecting(String id) {
        NodeConnectionState state = nodeState.get(id);
        return state != null && state.state == ConnectionState.CONNECTING;
    }

    /**
     * 输入已连接成功
     * @param id The connection identifier
     */
    public void connected(String id) {
        NodeConnectionState nodeState = nodeState(id);
        nodeState.state = ConnectionState.CONNECTED;
    }

    /**
     * 输入关闭连接
     * @param id  The connection we have disconnected
     * @param now The current time
     */
    public void disconnected(String id, long now) {
        NodeConnectionState nodeState = nodeState(id);
        nodeState.state = ConnectionState.DISCONNECTED;
        nodeState.lastConnectAttemptMs = now;
    }

    /**
     * 从跟踪的连接状态中删除给定节点。
     * Remove the given node from the tracked connection states.
     * The main difference between this and `disconnected` is the impact on `connectionDelay`: it will be 0 after this call whereas `reconnectBackoffMs` will be taken into account after `disconnected` is called.
     * @param id The connection to remove
     */
    public void remove(String id) {
        nodeState.remove(id);
    }

    /**
     * 获取一个节点的连接状态
     * @param id The id of the connection
     * @return The state of our connection
     */
    public ConnectionState connectionState(String id) {
        return nodeState(id).state;
    }

    /**
     * 获取一个节点的连接状态
     * @param id The connection to fetch the state for
     */
    private NodeConnectionState nodeState(String id) {
        NodeConnectionState state = this.nodeState.get(id);
        if (state == null) {
            throw new IllegalStateException("No entry found for connection " + id);
        }
        return state;
    }

    /**
     * The state of our connection to a node
     */
    private static class NodeConnectionState {

        ConnectionState state;
        /**
         * 最后一次尝试连接的时间戳
         */
        long lastConnectAttemptMs;

        public NodeConnectionState(ConnectionState state, long lastConnectAttempt) {
            this.state = state;
            this.lastConnectAttemptMs = lastConnectAttempt;
        }

        @Override
        public String toString() {
            return "NodeState(" + state + ", " + lastConnectAttemptMs + ")";
        }

    }

}
