/**
 * {@link DataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * Represents a contiguous block of data bytes that can be stored on a {@link AbstractMedium}. An {@link DataBlock}
 * basically has the following properties:
 * <ul>
 * <li>Is currently stored on exactly one or no {@link AbstractMedium}</li>
 * <li>Has a total size in bytes</li>
 * <li>Refers to a {@link DataBlockId} to identify the {@link DataBlock} which makes the {@link DataBlock} belong to
 * exactly one {@link DataFormat}.</li>
 * <li>Has zero or exactly one parent {@link DataBlock}</li>
 * <li>Has a {@link ByteOrder}, child blocks might however use different {@link ByteOrder}s than their parents</li>
 * <li>Has a {@link Charset}, child blocks might however use different {@link Charset}s than their parents</li>
 * </ul>
 *
 * An {@link DataBlock} need not be persistently stored on a {@link AbstractMedium} but can be only created in memory.
 * This is usually its first state after creation and before writing it to the {@link AbstractMedium}. Furthermore, it
 * may not have a parent. Such {@link DataBlock}s are said to be <i>top-level</i> {@link DataBlock}s. Physically, they
 * are stored on the first level of the {@link AbstractMedium} and need to be read first. Thus, {@link DataBlock}s form
 * a hierarchy of data where a single {@link DataBlock} consists of an arbitrary number of so-called <i>child</i>
 * {@link DataBlock}s. The children can only be read using derived interfaces.
 *
 * Note that this class does not support {@link DataBlock}s with a size larger than LONG_MAX, e.g. sizes larger than
 * 2^63-1 bytes. Such {@link DataBlock}s can only be read partly which produces incorrectly interpreted data read beyond
 * 2^63-1 bytes.
 *
 * An {@link DataBlock} may have an unknown size which is reflected by a return value of
 * {@link DataBlockDescription#UNKNOWN_SIZE} by the method {@link #getTotalSize()}.
 */
public interface DataBlock {

   /**
    * Returns the {@link AbstractMedium} the {@link DataBlock} is currently stored on. May return null if the
    * {@link DataBlock} has not been persisted yet.
    *
    * @return the {@link AbstractMedium} the {@link DataBlock} is currently stored on. May return null if the
    *         {@link DataBlock} has not been persisted yet.
    */
   public MediumOffset getMediumReference();

   /**
    * Returns the total size of the {@link DataBlock} in bytes.
    *
    * @return the total size of the {@link DataBlock} in bytes or {@link DataBlockDescription#UNKNOWN_SIZE} if the size
    *         of this {@link DataBlock} is unknown.
    */
   public long getTotalSize();

   /**
    * Reads raw bytes that build the {@link DataBlock}.
    *
    * @param offset
    *           the start offset from which to retrieve the bytes. The offset is given relative to the start byte of the
    *           {@link DataBlock} with index 0. The offset must not be equal to or bigger than the total size of
    *           {@link DataBlock}.
    * @param size
    *           the size of the bytes to read. Must not reach beyond the total size of the {@link DataBlock}.
    * @return the bytes that build this {@link DataBlock} from the given offset and size.
    */
   public byte[] getBytes(long offset, int size);

   /**
    * Returns the {@link DataBlockId} of the {@link DataBlock}.
    *
    * @return the id of the {@link DataBlock}.
    */
   public DataBlockId getId();

   /**
    * Returns the parent {@link DataBlock} of this instance. May return null if this is a top-level {@link DataBlock}.
    *
    * @return the parent of this {@link DataBlock} or null if this is a top-level {@link DataBlock}.
    */
   public DataBlock getParent();

   /**
    * Initially sets the parent {@link DataBlock} of this {@link DataBlock}. This method may only be called if a parent
    * is not already set, i.e. if #getParent() returns null.
    *
    * @param parent
    *           The parent to set.
    */
   public void initParent(DataBlock parent);

   /**
    * @param bytes
    */
   public void setBytes(byte[][] bytes);

   /**
    * Frees any internal data associated with this {@link DataBlock}. Should be called when you use large portions of
    * data or a lot of data blocks over a longer period of time to avoid out-of-memory conditions. Whenever an
    * {@link DataBlock} is not used anymore, call this method. However, after calling this method, the {@link DataBlock}
    * instance may still be used.
    */
   public void free();
}
