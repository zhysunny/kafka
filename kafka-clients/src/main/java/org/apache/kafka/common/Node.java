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
package org.apache.kafka.common;

/**
 * 一个节点相当于一个broker
 * @author 章云
 * @date 2019/12/19 14:18
 */
public class Node {

    /**
     * broker id
     */
    private final int id;
    /**
     * broker id
     */
    private final String idString;
    /**
     * ip或域名 host.name配置
     */
    private final String host;
    /**
     * 端口，port配置，默认9092
     */
    private final int port;

    public Node(int id, String host, int port) {
        super();
        this.id = id;
        this.idString = Integer.toString(id);
        this.host = host;
        this.port = port;
    }

    public static Node noNode() {
        return new Node(-1, "", -1);
    }

    /**
     * The node id of this node
     */
    public int id() {
        return id;
    }

    /**
     * String representation of the node id.
     * Typically the integer id is used to serialize over the wire, the string representation is used as an identifier with NetworkClient code
     */
    public String idString() {
        return idString;
    }

    /**
     * The host name for this node
     */
    public String host() {
        return host;
    }

    /**
     * The port for this node
     */
    public int port() {
        return port;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((host == null) ? 0 : host.hashCode());
        result = prime * result + id;
        result = prime * result + port;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node)obj;
        if (host == null) {
            if (other.host != null) {
                return false;
            }
        } else if (!host.equals(other.host)) {
            return false;
        }
        if (id != other.id) {
            return false;
        }
        if (port != other.port) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Node(" + id + ", " + host + ", " + port + ")";
    }

}
