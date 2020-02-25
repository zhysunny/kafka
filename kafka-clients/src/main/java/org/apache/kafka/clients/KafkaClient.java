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

import org.apache.kafka.common.Node;
import org.apache.kafka.common.protocol.ApiKeys;
import org.apache.kafka.common.requests.RequestHeader;
import java.io.Closeable;
import java.util.List;

/**
 * The interface for {@link NetworkClient}
 * @author 章云
 * @date 2020/2/9 22:00
 */
public interface KafkaClient extends Closeable {

    /**
     * 检查当前是否准备好向给定节点发送另一个请求，但是如果没有准备好，不要尝试连接。
     * @param node 要检查的节点
     * @param now  当前时间戳
     */
    boolean isReady(Node node, long now);

    /**
     * 启动到给定节点的连接(如果需要)，如果已经连接则返回true。
     * 只有在调用轮询时，节点的就绪状态才会更改。
     * @param node 要连接的节点。
     * @param now  当前时间戳
     * @return 如果我们准备立即开始向给定节点发送另一个请求，则为true
     */
    boolean ready(Node node, long now);

    /**
     * 根据连接状态返回尝试发送数据之前等待的毫秒数。
     * 断开连接时，这将考虑重新连接的回退时间。
     * 当连接或连接时，它处理慢速/停顿的连接。
     * @param node 要检查的节点
     * @param now  当前时间戳
     * @return 等待的毫秒数。
     */
    long connectionDelay(Node node, long now);

    /**
     * 根据连接状态检查节点的连接是否失败。
     * 这种连接失败通常是暂时的，可以在下一次{@link #ready(org.apache.kafka.common.Node, long)}}调用中恢复，但是在某些情况下，需要捕获暂时故障并对其进行重新处理。
     * @param node 要检查的节点
     * @return 如果连接失败且节点断开连接，则为true
     */
    boolean connectionFailed(Node node);

    /**
     * 将给定的请求排队发送。
     * 请求只能在就绪连接上发送。
     * @param request 请求
     * @param now     当前时间戳
     */
    void send(ClientRequest request, long now);

    /**
     * 从套接字执行实际的读写操作。
     * @param timeout 在ms中等待响应的最大时间量必须是非负的。实现可以在适当的情况下使用较低的值(其常见原因是较低的请求或元数据更新超时)
     * @param now     当前时间戳
     * @throws IllegalStateException 如果请求被发送到未准备好的节点
     */
    List<ClientResponse> poll(long timeout, long now);

    /**
     * 关闭到特定节点的连接(如果有的话)。
     * @param nodeId 节点的id
     */
    void close(String nodeId);

    /**
     * 选择未完成请求最少的节点。
     * 此方法将首选具有现有连接的节点，但如果所有现有连接都在使用，则可能选择尚未具有连接的节点。
     * @param now 当前时间戳
     * @return 正在运行的请求最少的节点。
     */
    Node leastLoadedNode(long now);

    /**
     * 我们尚未返回响应的当前正在运行的请求的数量
     */
    int inFlightRequestCount();

    /**
     * 获取特定节点的所有正在运行的请求
     * @param nodeId 节点的id
     */
    int inFlightRequestCount(String nodeId);

    /**
     * 为下一个请求生成一个请求头
     * @param key 请求的API键
     */
    RequestHeader nextRequestHeader(ApiKeys key);

    /**
     * 为给定的API密钥生成请求头
     * @param key     API键
     * @param version API版本
     * @return 具有适当的客户端id和相关id的请求头
     */
    RequestHeader nextRequestHeader(ApiKeys key, short version);

    /**
     * 如果当前正在等待I/O而被阻塞，则唤醒客户机
     */
    void wakeup();

}
