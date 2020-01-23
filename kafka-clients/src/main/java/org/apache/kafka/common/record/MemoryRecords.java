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
package org.apache.kafka.common.record;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.utils.AbstractIterator;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

/**
 * A {@link Records} implementation backed by a ByteBuffer.
 */
public class MemoryRecords implements Records {

    private final static int WRITE_LIMIT_FOR_READABLE_ONLY = -1;

    /**
     * 只用于附件的压缩机
     */
    private final Compressor compressor;

    /**
     * 可写缓冲区的写限制，它可能小于缓冲区容量
     */
    private final int writeLimit;

    /**
     * 初始缓冲区的容量，仅用于取消可写记录的分配
     */
    private final int initialCapacity;

    /**
     * 用于读取的底层缓冲区;虽然记录仍然是可写的，但它是空的
     */
    private ByteBuffer buffer;

    /**
     * 指示内存记录是否可写(即用于追加或只读)
     */
    private boolean writable;

    /**
     * 构造一个可写的内存记录
     * @param buffer
     * @param type
     * @param writable
     * @param writeLimit
     */
    private MemoryRecords(ByteBuffer buffer, CompressionType type, boolean writable, int writeLimit) {
        this.writable = writable;
        this.writeLimit = writeLimit;
        this.initialCapacity = buffer.capacity();
        if (this.writable) {
            this.buffer = null;
            this.compressor = new Compressor(buffer, type);
        } else {
            this.buffer = buffer;
            this.compressor = null;
        }
    }

    public static MemoryRecords emptyRecords(ByteBuffer buffer, CompressionType type, int writeLimit) {
        return new MemoryRecords(buffer, type, true, writeLimit);
    }

    public static MemoryRecords emptyRecords(ByteBuffer buffer, CompressionType type) {
        // 使用缓冲区容量作为默认的写限制
        return emptyRecords(buffer, type, buffer.capacity());
    }

    public static MemoryRecords readableRecords(ByteBuffer buffer) {
        return new MemoryRecords(buffer, CompressionType.NONE, false, WRITE_LIMIT_FOR_READABLE_ONLY);
    }

    /**
     * 将给定的记录和偏移量追加到缓冲区
     */
    public void append(long offset, Record record) {
        if (!writable) {
            throw new IllegalStateException("Memory records is not writable");
        }

        int size = record.size();
        compressor.putLong(offset);
        compressor.putInt(size);
        compressor.put(record.buffer());
        compressor.recordWritten(size + Records.LOG_OVERHEAD);
        record.buffer().rewind();
    }

    /**
     * 将给定的记录和偏移量追加到缓冲区
     */
    public void append(long offset, byte[] key, byte[] value) {
        if (!writable) {
            throw new IllegalStateException("Memory records is not writable");
        }

        int size = Record.recordSize(key, value);
        compressor.putLong(offset);
        compressor.putInt(size);
        compressor.putRecord(key, value);
        compressor.recordWritten(size + Records.LOG_OVERHEAD);
    }

    /**
     * 检查是否有空间存放包含给定键/值对的新记录
     * 请注意，返回值是基于写入到压缩器的字节的估计值，如果确实使用了压缩，那么这个估计值可能并不准确。
     * 当这种情况发生时，以下附加可能会导致底层字节缓冲区流中的动态缓冲区重新分配。
     * 有一个例外情况，当附加的单个消息的大小大于批处理大小时，容量将是大于写限制的消息大小，即批处理大小。在这种情况下，检查应该基于已初始化的缓冲区的容量，而不是写限制，以便接受这条记录。
     */
    public boolean hasRoomFor(byte[] key, byte[] value) {
        return this.writable && this.compressor.numRecordsWritten() == 0 ?
        this.initialCapacity >= Records.LOG_OVERHEAD + Record.recordSize(key, value) :
        this.writeLimit >= this.compressor.estimatedBytesWritten() + Records.LOG_OVERHEAD + Record.recordSize(key, value);
    }

    public boolean isFull() {
        return !this.writable || this.writeLimit <= this.compressor.estimatedBytesWritten();
    }

    /**
     * Close this batch for no more appends
     */
    public void close() {
        if (writable) {
            // close the compressor to fill-in wrapper message metadata if necessary
            compressor.close();

            // flip the underlying buffer to be ready for reads
            buffer = compressor.buffer();
            buffer.flip();

            // reset the writable flag
            writable = false;
        }
    }

    /**
     * The size of this record set
     */
    @Override
    public int sizeInBytes() {
        if (writable) {
            return compressor.buffer().position();
        } else {
            return buffer.limit();
        }
    }

    /**
     * The compression rate of this record set
     */
    public double compressionRate() {
        if (compressor == null) {
            return 1.0;
        } else {
            return compressor.compressionRate();
        }
    }

    /**
     * 返回初始缓冲区的容量，对于可写记录，它可能与当前缓冲区的容量不同
     */
    public int initialCapacity() {
        return this.initialCapacity;
    }

    /**
     * Get the byte buffer that backs this records instance for reading
     */
    public ByteBuffer buffer() {
        if (writable) {
            throw new IllegalStateException("The memory records must not be writable any more before getting its underlying buffer");
        }

        return buffer.duplicate();
    }

    @Override
    public Iterator<LogEntry> iterator() {
        if (writable) {
            // flip on a duplicate buffer for reading
            return new RecordsIterator((ByteBuffer)this.buffer.duplicate().flip(), CompressionType.NONE, false);
        } else {
            // do not need to flip for non-writable buffer
            return new RecordsIterator(this.buffer.duplicate(), CompressionType.NONE, false);
        }
    }

    @Override
    public String toString() {
        Iterator<LogEntry> iter = iterator();
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        while (iter.hasNext()) {
            LogEntry entry = iter.next();
            builder.append('(');
            builder.append("offset=");
            builder.append(entry.offset());
            builder.append(",");
            builder.append("record=");
            builder.append(entry.record());
            builder.append(")");
        }
        builder.append(']');
        return builder.toString();
    }

    public static class RecordsIterator extends AbstractIterator<LogEntry> {

        private final ByteBuffer buffer;
        private final DataInputStream stream;
        private final CompressionType type;
        private final boolean shallow;
        private RecordsIterator innerIter;

        public RecordsIterator(ByteBuffer buffer, CompressionType type, boolean shallow) {
            this.type = type;
            this.buffer = buffer;
            this.shallow = shallow;
            this.stream = Compressor.wrapForInput(new ByteBufferInputStream(this.buffer), type);
        }

        /*
         * Read the next record from the buffer.
         *
         * Note that in the compressed message set, each message value size is set as the size of the un-compressed
         * version of the message value, so when we do de-compression allocating an array of the specified size for
         * reading compressed value data is sufficient.
         */
        @Override
        protected LogEntry makeNext() {
            if (innerDone()) {
                try {
                    // read the offset
                    long offset = stream.readLong();
                    // read record size
                    int size = stream.readInt();
                    if (size < 0) {
                        throw new IllegalStateException("Record with size " + size);
                    }
                    // read the record, if compression is used we cannot depend on size
                    // and hence has to do extra copy
                    ByteBuffer rec;
                    if (type == CompressionType.NONE) {
                        rec = buffer.slice();
                        int newPos = buffer.position() + size;
                        if (newPos > buffer.limit()) {
                            return allDone();
                        }
                        buffer.position(newPos);
                        rec.limit(size);
                    } else {
                        byte[] recordBuffer = new byte[size];
                        stream.readFully(recordBuffer, 0, size);
                        rec = ByteBuffer.wrap(recordBuffer);
                    }
                    LogEntry entry = new LogEntry(offset, new Record(rec));

                    // decide whether to go shallow or deep iteration if it is compressed
                    CompressionType compression = entry.record().compressionType();
                    if (compression == CompressionType.NONE || shallow) {
                        return entry;
                    } else {
                        // init the inner iterator with the value payload of the message,
                        // which will de-compress the payload to a set of messages;
                        // since we assume nested compression is not allowed, the deep iterator
                        // would not try to further decompress underlying messages
                        ByteBuffer value = entry.record().value();
                        innerIter = new RecordsIterator(value, compression, true);
                        return innerIter.next();
                    }
                } catch (EOFException e) {
                    return allDone();
                } catch (IOException e) {
                    throw new KafkaException(e);
                }
            } else {
                return innerIter.next();
            }
        }

        private boolean innerDone() {
            return innerIter == null || !innerIter.hasNext();
        }

    }

}
