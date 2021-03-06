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
package org.apache.kafka.common.record;

import org.apache.kafka.common.utils.Crc32;
import org.apache.kafka.common.utils.Utils;
import java.nio.ByteBuffer;

/**
 * 记录:一个序列化的键和值，以及相关的CRC和其他字段
 * @author 章云
 * @date 2020/1/22 16:36
 */
public final class Record {

    /**
     * The current offset and size for all the fixed-length fields
     */
    public static final int CRC_OFFSET = 0;
    public static final int CRC_LENGTH = 4;
    public static final int MAGIC_OFFSET = CRC_OFFSET + CRC_LENGTH;//4
    public static final int MAGIC_LENGTH = 1;
    public static final int ATTRIBUTES_OFFSET = MAGIC_OFFSET + MAGIC_LENGTH;//5
    public static final int ATTRIBUTE_LENGTH = 1;
    public static final int KEY_SIZE_OFFSET = ATTRIBUTES_OFFSET + ATTRIBUTE_LENGTH;//6
    public static final int KEY_SIZE_LENGTH = 4;
    public static final int KEY_OFFSET = KEY_SIZE_OFFSET + KEY_SIZE_LENGTH;//10
    public static final int VALUE_SIZE_LENGTH = 4;

    /**
     * The size for the record header
     */
    public static final int HEADER_SIZE = CRC_LENGTH + MAGIC_LENGTH + ATTRIBUTE_LENGTH;//6

    /**
     * The amount of overhead bytes in a record
     */
    public static final int RECORD_OVERHEAD = HEADER_SIZE + KEY_SIZE_LENGTH + VALUE_SIZE_LENGTH;//14

    /**
     * The current "magic" value
     */
    public static final byte CURRENT_MAGIC_VALUE = 0;

    /**
     * Specifies the mask for the compression code. 3 bits to hold the compression codec. 0 is reserved to indicate no
     * compression
     */
    public static final int COMPRESSION_CODEC_MASK = 0x07;//7

    /**
     * Compression code for uncompressed records
     */
    public static final int NO_COMPRESSION = 0;

    private final ByteBuffer buffer;

    public Record(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    /**
     * 创建日志记录的构造函数。
     * 如果记录的压缩类型不是none，那么它的值负载应该已经用指定的类型压缩过了;
     * 构造函数将始终按原样写入值payload，而不会执行压缩本身。
     * @param key         The key of the record (null, if none)
     * @param value       The record value
     * @param type        The compression type used on the contents of the record (if any)
     * @param valueOffset The offset into the payload array used to extract payload
     * @param valueSize   The size of the payload to use
     */
    public Record(byte[] key, byte[] value, CompressionType type, int valueOffset, int valueSize) {
        this(ByteBuffer
        .allocate(recordSize(key == null ? 0 : key.length, value == null ? 0 : valueSize >= 0 ? valueSize : value.length - valueOffset)));
        write(this.buffer, key, value, type, valueOffset, valueSize);
        this.buffer.rewind();
    }

    public Record(byte[] key, byte[] value, CompressionType type) {
        this(key, value, type, 0, -1);
    }

    public Record(byte[] value, CompressionType type) {
        this(null, value, type);
    }

    public Record(byte[] key, byte[] value) {
        this(key, value, CompressionType.NONE);
    }

    public Record(byte[] value) {
        this(null, value, CompressionType.NONE);
    }

    /**
     * 向缓冲区写入一条记录，如果记录的压缩类型为none，则其值有效负载应该已经使用指定的类型进行了压缩
     * @param buffer
     * @param key
     * @param value
     * @param type
     * @param valueOffset
     * @param valueSize
     */
    public static void write(ByteBuffer buffer, byte[] key, byte[] value, CompressionType type, int valueOffset, int valueSize) {
        // 构造压缩类型为none的压缩机，因为该函数不执行任何操作
        // 根据输入类型进行压缩，它将仅按原样写入记录的有效负载
        Compressor compressor = new Compressor(buffer, CompressionType.NONE, buffer.capacity());
        compressor.putRecord(key, value, type, valueOffset, valueSize);
    }

    public static void write(Compressor compressor, long crc, byte attributes, byte[] key, byte[] value, int valueOffset, int valueSize) {
        // write crc
        compressor.putInt((int)(crc & 0xffffffffL));
        // write magic value
        compressor.putByte(CURRENT_MAGIC_VALUE);
        // write attributes
        compressor.putByte(attributes);
        // write the key
        if (key == null) {
            compressor.putInt(-1);
        } else {
            compressor.putInt(key.length);
            compressor.put(key, 0, key.length);
        }
        // write the value
        if (value == null) {
            compressor.putInt(-1);
        } else {
            int size = valueSize >= 0 ? valueSize : (value.length - valueOffset);
            compressor.putInt(size);
            compressor.put(value, valueOffset, size);
        }
    }

    public static int recordSize(byte[] key, byte[] value) {
        return recordSize(key == null ? 0 : key.length, value == null ? 0 : value.length);
    }

    public static int recordSize(int keySize, int valueSize) {
        return RECORD_OVERHEAD + keySize + valueSize;
    }

    public ByteBuffer buffer() {
        return this.buffer;
    }

    public static byte computeAttributes(CompressionType type) {
        byte attributes = 0;
        if (type.id > 0) {
            attributes = (byte)(attributes | (COMPRESSION_CODEC_MASK & type.id));
        }
        return attributes;
    }

    /**
     * Compute the checksum of the record from the record contents
     */
    public static long computeChecksum(ByteBuffer buffer, int position, int size) {
        Crc32 crc = new Crc32();
        crc.update(buffer.array(), buffer.arrayOffset() + position, size);
        return crc.getValue();
    }

    /**
     * Compute the checksum of the record from the attributes, key and value payloads
     */
    public static long computeChecksum(byte[] key, byte[] value, CompressionType type, int valueOffset, int valueSize) {
        Crc32 crc = new Crc32();
        crc.update(CURRENT_MAGIC_VALUE);
        byte attributes = 0;
        if (type.id > 0) {
            attributes = (byte)(attributes | (COMPRESSION_CODEC_MASK & type.id));
        }
        crc.update(attributes);
        // update for the key
        if (key == null) {
            crc.updateInt(-1);
        } else {
            crc.updateInt(key.length);
            crc.update(key, 0, key.length);
        }
        // update for the value
        if (value == null) {
            crc.updateInt(-1);
        } else {
            int size = valueSize >= 0 ? valueSize : (value.length - valueOffset);
            crc.updateInt(size);
            crc.update(value, valueOffset, size);
        }
        return crc.getValue();
    }

    /**
     * 从记录内容计算记录的校验和
     */
    public long computeChecksum() {
        return computeChecksum(buffer, MAGIC_OFFSET, buffer.limit() - MAGIC_OFFSET);
    }

    /**
     * 检索此记录的先前计算的CRC
     */
    public long checksum() {
        return Utils.readUnsignedInt(buffer, CRC_OFFSET);
    }

    /**
     * Returns true if the crc stored with the record matches the crc computed off the record contents
     */
    public boolean isValid() {
        return checksum() == computeChecksum();
    }

    /**
     * Throw an InvalidRecordException if isValid is false for this record
     */
    public void ensureValid() {
        if (!isValid()) {
            throw new InvalidRecordException("Record is corrupt (stored crc = " + checksum()
            + ", computed crc = "
            + computeChecksum()
            + ")");
        }
    }

    /**
     * The complete serialized size of this record in bytes (including crc, header attributes, etc)
     */
    public int size() {
        return buffer.limit();
    }

    /**
     * The length of the key in bytes
     */
    public int keySize() {
        return buffer.getInt(KEY_SIZE_OFFSET);
    }

    /**
     * Does the record have a key?
     */
    public boolean hasKey() {
        return keySize() >= 0;
    }

    /**
     * The position where the value size is stored
     */
    private int valueSizeOffset() {
        return KEY_OFFSET + Math.max(0, keySize());
    }

    /**
     * The length of the value in bytes
     */
    public int valueSize() {
        return buffer.getInt(valueSizeOffset());
    }

    /**
     * The magic version of this record
     */
    public byte magic() {
        return buffer.get(MAGIC_OFFSET);
    }

    /**
     * The attributes stored with this record
     */
    public byte attributes() {
        return buffer.get(ATTRIBUTES_OFFSET);
    }

    /**
     * The compression type used with this record
     */
    public CompressionType compressionType() {
        return CompressionType.forId(buffer.get(ATTRIBUTES_OFFSET) & COMPRESSION_CODEC_MASK);
    }

    /**
     * A ByteBuffer containing the value of this record
     */
    public ByteBuffer value() {
        return sliceDelimited(valueSizeOffset());
    }

    /**
     * A ByteBuffer containing the message key
     */
    public ByteBuffer key() {
        return sliceDelimited(KEY_SIZE_OFFSET);
    }

    /**
     * Read a size-delimited byte buffer starting at the given offset
     */
    private ByteBuffer sliceDelimited(int start) {
        int size = buffer.getInt(start);
        if (size < 0) {
            return null;
        } else {
            ByteBuffer b = buffer.duplicate();
            b.position(start + 4);
            b = b.slice();
            b.limit(size);
            b.rewind();
            return b;
        }
    }

    @Override
    public String toString() {
        return String.format("Record(magic = %d, attributes = %d, compression = %s, crc = %d, key = %d bytes, value = %d bytes)",
        magic(),
        attributes(),
        compressionType(),
        checksum(),
        key() == null ? 0 : key().limit(),
        value() == null ? 0 : value().limit());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (!other.getClass().equals(Record.class)) {
            return false;
        }
        Record record = (Record)other;
        return this.buffer.equals(record.buffer);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

}
