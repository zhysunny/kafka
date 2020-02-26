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
package org.apache.kafka.clients.consumer.internals;

/**
 * 用于将心跳管理到协调器的助手类
 * @author 章云
 * @date 2020/2/26 13:36
 */
public final class Heartbeat {

    /**
     * 超时时间,session.timeout.ms  default=30*1000
     */
    private final long timeout;
    /**
     * 心跳间隔时间,heartbeat.interval.ms  default=3*1000
     */
    private final long interval;
    /**
     * 最后一次心跳发送时间戳
     */
    private long lastHeartbeatSend;
    /**
     * 最后一次心跳接收时间戳
     */
    private long lastHeartbeatReceive;
    /**
     * 超时时间开始计算的时间
     */
    private long lastSessionReset;

    public Heartbeat(long timeout, long interval, long now) {
        if (interval >= timeout) {
            throw new IllegalArgumentException("Heartbeat must be set lower than the session timeout");
        }
        this.timeout = timeout;
        this.interval = interval;
        this.lastSessionReset = now;
    }

    public void sentHeartbeat(long now) {
        this.lastHeartbeatSend = now;
    }

    public void receiveHeartbeat(long now) {
        this.lastHeartbeatReceive = now;
    }

    public boolean shouldHeartbeat(long now) {
        return timeToNextHeartbeat(now) == 0;
    }
    
    public long lastHeartbeatSend() {
        return this.lastHeartbeatSend;
    }

    public long timeToNextHeartbeat(long now) {
        long timeSinceLastHeartbeat = now - Math.max(lastHeartbeatSend, lastSessionReset);

        if (timeSinceLastHeartbeat > interval) {
            return 0;
        } else {
            return interval - timeSinceLastHeartbeat;
        }
    }

    public boolean sessionTimeoutExpired(long now) {
        return now - Math.max(lastSessionReset, lastHeartbeatReceive) > timeout;
    }

    public long interval() {
        return interval;
    }

    public void resetSessionTimeout(long now) {
        this.lastSessionReset = now;
    }

}