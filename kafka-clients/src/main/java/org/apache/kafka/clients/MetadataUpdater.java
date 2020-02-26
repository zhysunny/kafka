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
import org.apache.kafka.common.protocol.types.Struct;
import java.util.List;

/**
 * “NetworkClient”用于请求更新集群元数据信息并从此类元数据中检索集群节点的接口。
 * 这是一个内部类。
 * <p>
 * 这个类不是线程安全的!
 */
interface MetadataUpdater {

    /**
     * 获取当前集群信息而不阻塞。
     * @return
     */
    List<Node> fetchNodes();

    /**
     * 如果要更新集群元数据信息，则返回true。
     * @param now
     * @return
     */
    boolean isUpdateDue(long now);

    /**
     * 如果需要并且可能，启动集群元数据更新。
     * 返回元数据更新之前的时间(如果由于调用而启动了更新，则该时间为0)。
     * 如果实现依赖于“NetworkClient”发送请求，则完成的receive将被传递给“maybeHandleCompletedReceive”。
     * “needed”和“possible”的语义依赖于实现，可能会考虑许多因素，如节点可用性、上一次元数据更新的时间等。
     * @param now
     * @return
     */
    long maybeUpdate(long now);

    /**
     * 如果'request'是一个元数据请求，处理它并返回'true'。否则返回“false”。
     * <p>
     * 这为“MetadataUpdater”实现提供了一种机制，可以将NetworkClient实例用于自己的请求，并对此类请求的断开进行特殊处理。
     * @param request
     * @return
     */
    boolean maybeHandleDisconnection(ClientRequest request);

    /**
     * 如果'request'是一个元数据请求，处理它并返回'true'。否则,返回“false”。
     * <p>
     * 这为“MetadataUpdater”实现提供了一种机制，可以将NetworkClient实例用于自己的请求，并对此类请求的完整接收进行特殊处理。
     * @param request
     * @param now
     * @param body
     * @return
     */
    boolean maybeHandleCompletedReceive(ClientRequest request, long now, Struct body);

    /**
     * Schedules an update of the current cluster metadata info. A subsequent call to `maybeUpdate` would trigger the
     * start of the update if possible (see `maybeUpdate` for more information).
     */
    void requestUpdate();

}
