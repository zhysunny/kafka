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

import org.apache.kafka.common.protocol.types.Struct;

/**
 * 来自服务器的响应。既包含响应的主体，也包含最初发送的相关请求。
 * @author 章云
 * @date 2020/2/26 10:50
 */
public class ClientResponse {

    /**
     * 接收此响应时的时间戳
     */
    private final long receivedTimeMs;
    /**
     * 客户端是否在完全读取响应之前断开连接
     */
    private final boolean disconnected;
    /**
     * 原始请求
     */
    private final ClientRequest request;
    /**
     * 如果我们断开连接则响应内容，不期望响应，则为null
     */
    private final Struct responseBody;

    /**
     * @param request        The original request
     * @param receivedTimeMs The unix timestamp when this response was received
     * @param disconnected   Whether the client disconnected before fully reading a response
     * @param responseBody   The response contents (or null) if we disconnected or no response was expected
     */
    public ClientResponse(ClientRequest request, long receivedTimeMs, boolean disconnected, Struct responseBody) {
        super();
        this.receivedTimeMs = receivedTimeMs;
        this.disconnected = disconnected;
        this.request = request;
        this.responseBody = responseBody;
    }

    public long receivedTimeMs() {
        return receivedTimeMs;
    }

    public boolean wasDisconnected() {
        return disconnected;
    }

    public ClientRequest request() {
        return request;
    }

    public Struct responseBody() {
        return responseBody;
    }

    public boolean hasResponse() {
        return responseBody != null;
    }

    public long requestLatencyMs() {
        return receivedTimeMs() - this.request.createdTimeMs();
    }

    @Override
    public String toString() {
        return "ClientResponse(receivedTimeMs=" + receivedTimeMs +
        ", disconnected=" +
        disconnected +
        ", request=" +
        request +
        ", responseBody=" +
        responseBody +
        ")";
    }

}
