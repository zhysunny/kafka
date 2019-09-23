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
 * 用于添加简单同步以提供线程安全缓存的缓存包装器。
 * 注意，这只是在底层非同步缓存的每个缓存方法周围添加同步。
 * 它不支持原子检查条目的存在性，也不支持计算和插入缺失的值。
 * @author 章云
 * @date 2019/9/23 21:37
 */
public class SynchronizedCache<K, V> implements Cache<K, V> {

    private final Cache<K, V> underlying;

    public SynchronizedCache(Cache<K, V> underlying) {
        this.underlying = underlying;
    }

    @Override
    public synchronized V get(K key) {
        return underlying.get(key);
    }

    @Override
    public synchronized void put(K key, V value) {
        underlying.put(key, value);
    }

    @Override
    public synchronized boolean remove(K key) {
        return underlying.remove(key);
    }

    @Override
    public synchronized long size() {
        return underlying.size();
    }

}
