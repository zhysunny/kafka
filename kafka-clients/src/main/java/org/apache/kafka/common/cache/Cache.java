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
 **/

package org.apache.kafka.common.cache;

/**
 * 用于缓存的接口、半支架映射，它存储键值映射，直到满足清除条件或条目手动失效。
 * 缓存不需要是线程安全的，但是一些实现可能是线程安全的。
 */
public interface Cache<K, V> {

    /**
     * Look up a value in the cache.
     * @param key the key to
     * @return the cached value, or null if it is not present.
     */
    V get(K key);

    /**
     * Insert an entry into the cache.
     * @param key   the key to insert
     * @param value the value to insert
     */
    void put(K key, V value);

    /**
     * Manually invalidate a key, clearing its entry from the cache.
     * @param key the key to remove
     * @return true if the key existed in the cache and the entry was removed or false if it was not present
     */
    boolean remove(K key);

    /**
     * Get the number of entries in this cache. If this cache is used by multiple threads concurrently, the returned
     * value will only be approximate.
     * @return the number of entries in the cache
     */
    long size();

}
