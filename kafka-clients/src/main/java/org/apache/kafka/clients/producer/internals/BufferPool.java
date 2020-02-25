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
 */
package org.apache.kafka.clients.producer.internals;

import org.apache.kafka.common.MetricName;
import org.apache.kafka.common.errors.TimeoutException;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.metrics.Sensor;
import org.apache.kafka.common.metrics.stats.Rate;
import org.apache.kafka.common.utils.Time;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 保持在给定内存限制下的字节缓冲区池。
 * <p>
 * 该类相当特定于生产者的需要。
 * <p>
 * 特别是它具有以下性质:
 * <p>
 * 有一个特殊的“池大小”和缓冲区的大小保持在一个自由列表和回收
 * <p>
 * 这是公平的。也就是说，所有内存都分配给等待时间最长的线程，直到它有足够的内存。
 * <p>
 * 这可以防止在线程请求大量内存并且需要阻塞多个缓冲区才能释放时出现死锁。
 * @author 章云
 * @date 2020/2/3 9:36
 */
public final class BufferPool {

    /**
     * 此缓冲池可分配的最大内存量，默认32M，配置项buffer.memory
     */
    private final long totalMemory;
    /**
     * 可用内存大小
     */
    private long availableMemory;
    /**
     * 要在空闲列表中缓存而不是释放的缓冲区大小，默认16K，配置项batch.size
     */
    private final int poolableSize;
    private final ReentrantLock lock;
    private final Deque<ByteBuffer> free;
    private final Deque<Condition> waiters;
    /**
     * KafkaProducer中创建的实例
     */
    private final Metrics metrics;
    private final Time time;
    private final Sensor waitTime;

    /**
     * Create a new buffer pool
     * @param memory        The maximum amount of memory that this buffer pool can allocate
     * @param poolableSize  The buffer size to cache in the free list rather than deallocating
     * @param metrics       instance of Metrics
     * @param time          time instance
     * @param metricGrpName logical group name for metrics
     * @param metricTags    additional key/val attributes for metrics
     */
    public BufferPool(long memory, int poolableSize, Metrics metrics, Time time, String metricGrpName, Map<String, String> metricTags) {
        this.poolableSize = poolableSize;
        this.lock = new ReentrantLock();
        this.free = new ArrayDeque<ByteBuffer>();
        this.waiters = new ArrayDeque<Condition>();
        this.totalMemory = memory;
        this.availableMemory = memory;
        this.metrics = metrics;
        this.time = time;
        this.waitTime = this.metrics.sensor("bufferpool-wait-time");
        MetricName metricName = new MetricName("bufferpool-wait-ratio",
        // producer-metrics
        metricGrpName,
        "The fraction of time an appender waits for space allocation.",
        // client-id
        metricTags);
        this.waitTime.add(metricName, new Rate(TimeUnit.NANOSECONDS));
    }

    /**
     * Allocate a buffer of the given size. This method blocks if there is not enough memory and the buffer pool
     * is configured with blocking mode.
     * @param size           The buffer size to allocate in bytes
     * @param maxTimeToBlock The maximum time in milliseconds to block for buffer memory to be available
     * @return The buffer
     * @throws InterruptedException     If the thread is interrupted while blocked
     * @throws IllegalArgumentException if size is larger than the total memory controlled by the pool (and hence we would block
     *                                  forever)
     */
    public ByteBuffer allocate(int size, long maxTimeToBlock) throws InterruptedException {
        if (size > this.totalMemory) {
            throw new IllegalArgumentException("Attempt to allocate " + size
            + " bytes, but there is a hard limit of "
            + this.totalMemory
            + " on memory allocations.");
        }

        this.lock.lock();
        try {
            // check if we have a free buffer of the right size pooled
            if (size == poolableSize && !this.free.isEmpty()) {
                return this.free.pollFirst();
            }

            // now check if the request is immediately satisfiable with the
            // memory on hand or if we need to block
            int freeListSize = this.free.size() * this.poolableSize;
            if (this.availableMemory + freeListSize >= size) {
                // we have enough unallocated or pooled memory to immediately
                // satisfy the request
                freeUp(size);
                this.availableMemory -= size;
                lock.unlock();
                return ByteBuffer.allocate(size);
            } else {
                // we are out of memory and will have to block
                int accumulated = 0;
                ByteBuffer buffer = null;
                Condition moreMemory = this.lock.newCondition();
                this.waiters.addLast(moreMemory);
                // loop over and over until we have a buffer or have reserved
                // enough memory to allocate one
                while (accumulated < size) {
                    long startWait = time.nanoseconds();
                    if (!moreMemory.await(maxTimeToBlock, TimeUnit.MILLISECONDS)) {
                        throw new TimeoutException("Failed to allocate memory within the configured max blocking time");
                    }
                    long endWait = time.nanoseconds();
                    this.waitTime.record(endWait - startWait, time.milliseconds());

                    // check if we can satisfy this request from the free list,
                    // otherwise allocate memory
                    if (accumulated == 0 && size == this.poolableSize && !this.free.isEmpty()) {
                        // just grab a buffer from the free list
                        buffer = this.free.pollFirst();
                        accumulated = size;
                    } else {
                        // we'll need to allocate memory, but we may only get
                        // part of what we need on this iteration
                        freeUp(size - accumulated);
                        int got = (int)Math.min(size - accumulated, this.availableMemory);
                        this.availableMemory -= got;
                        accumulated += got;
                    }
                }

                // remove the condition for this thread to let the next thread
                // in line start getting memory
                Condition removed = this.waiters.removeFirst();
                if (removed != moreMemory) {
                    throw new IllegalStateException("Wrong condition: this shouldn't happen.");
                }

                // signal any additional waiters if there is more memory left
                // over for them
                if (this.availableMemory > 0 || !this.free.isEmpty()) {
                    if (!this.waiters.isEmpty()) {
                        this.waiters.peekFirst().signal();
                    }
                }

                // unlock and return the buffer
                lock.unlock();
                if (buffer == null) {
                    return ByteBuffer.allocate(size);
                } else {
                    return buffer;
                }
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * Attempt to ensure we have at least the requested number of bytes of memory for allocation by deallocating pooled
     * buffers (if needed)
     */
    private void freeUp(int size) {
        while (!this.free.isEmpty() && this.availableMemory < size) {
            this.availableMemory += this.free.pollLast().capacity();
        }
    }

    /**
     * Return buffers to the pool. If they are of the poolable size add them to the free list, otherwise just mark the
     * memory as free.
     * @param buffer The buffer to return
     * @param size   The size of the buffer to mark as deallocated, note that this maybe smaller than buffer.capacity
     *               since the buffer may re-allocate itself during in-place compression
     */
    public void deallocate(ByteBuffer buffer, int size) {
        lock.lock();
        try {
            if (size == this.poolableSize && size == buffer.capacity()) {
                buffer.clear();
                this.free.add(buffer);
            } else {
                this.availableMemory += size;
            }
            Condition moreMem = this.waiters.peekFirst();
            if (moreMem != null) {
                moreMem.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void deallocate(ByteBuffer buffer) {
        deallocate(buffer, buffer.capacity());
    }

    /**
     * the total free memory both unallocated and in the free list
     */
    public long availableMemory() {
        lock.lock();
        try {
            return this.availableMemory + this.free.size() * this.poolableSize;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Get the unallocated memory (not in the free list or in use)
     */
    public long unallocatedMemory() {
        lock.lock();
        try {
            return this.availableMemory;
        } finally {
            lock.unlock();
        }
    }

    /**
     * The number of threads blocked waiting on memory
     */
    public int queued() {
        lock.lock();
        try {
            return this.waiters.size();
        } finally {
            lock.unlock();
        }
    }

    /**
     * The buffer size that will be retained in the free list after use
     */
    public int poolableSize() {
        return this.poolableSize;
    }

    /**
     * The total memory managed by this pool
     */
    public long totalMemory() {
        return this.totalMemory;
    }
}