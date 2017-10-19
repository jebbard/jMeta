/**
 * {@link IDataBlock}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.type;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.media.api.type.AbstractMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;

/**
 * Represents a contiguous block of data bytes that can be stored on a {@link AbstractMedium}. An {@link IDataBlock}
 * basically has the following properties:
 * <li>
 * <ul>
 * Is currently stored on exactly one or no {@link AbstractMedium}
 * </ul>
 * <ul>
 * Has a total size in bytes
 * </ul>
 * <ul>
 * Refers to a {@link DataBlockId} to identify the {@link IDataBlock} which makes the {@link IDataBlock} belong to
 * exactly one {@link DataFormat}.
 * </ul>
 * <ul>
 * Has zero or exactly one parent {@link IDataBlock}
 * </ul>
 * <ul>
 * Has a {@link ByteOrder}, child blocks might however use different {@link ByteOrder}s than their parents
 * </ul>
 * <ul>
 * Has a {@link Charset}, child blocks might however use different {@link Charset}s than their parents
 * </ul>
 * </li>
 *
 * An {@link IDataBlock} need not be persistently stored on a {@link AbstractMedium} but can be only created in memory.
 * This is usually its first state after creation and before writing it to the {@link AbstractMedium}. Furthermore, it
 * may not have a parent. Such {@link IDataBlock}s are said to be <i>top-level</i> {@link IDataBlock}s. Physically, they
 * are stored on the first level of the {@link AbstractMedium} and need to be read first. Thus, {@link IDataBlock}s form
 * a hierarchy of data where a single {@link IDataBlock} consists of an arbitrary number of so-called <i>child</i>
 * {@link IDataBlock}s. The children can only be read using derived interfaces.
 *
 * Note that this class does not support {@link IDataBlock}s with a size larger than LONG_MAX, e.g. sizes larger than
 * 2^63-1 bytes. Such {@link IDataBlock}s can only be read partly which produces incorrectly interpreted data read
 * beyond 2^63-1 bytes.
 *
 * An {@link IDataBlock} may have an unknown size which is reflected by a return value of
 * {@link DataBlockDescription#UNKNOWN_SIZE} by the method {@link #getTotalSize()}.
 */
public interface IDataBlock {

   /**
    * Returns the {@link AbstractMedium} the {@link IDataBlock} is currently stored on. May return null if the
    * {@link IDataBlock} has not been persisted yet.
    *
    * @return the {@link AbstractMedium} the {@link IDataBlock} is currently stored on. May return null if the
    *         {@link IDataBlock} has not been persisted yet.
    */
   public IMediumReference getMediumReference();

   /**
    * Returns the total size of the {@link IDataBlock} in bytes.
    *
    * @return the total size of the {@link IDataBlock} in bytes or {@link DataBlockDescription#UNKNOWN_SIZE} if the size
    *         of this {@link IDataBlock} is unknown.
    */
   public long getTotalSize();

   /**
    * Reads raw bytes that build the {@link IDataBlock}.
    *
    * @param offset
    *           the start offset from which to retrieve the bytes. The offset is given relative to the start byte of the
    *           {@link IDataBlock} with index 0. The offset must not be equal to or bigger than the total size of
    *           {@link IDataBlock}.
    * @param size
    *           the size of the bytes to read. Must not reach beyond the total size of the {@link IDataBlock}.
    * @return the bytes that build this {@link IDataBlock} from the given offset and size.
    *
    * @pre offset + size < #getTotalSize()
    * @pre offset >= 0
    * @pre size >= 0
    */
   public byte[] getBytes(long offset, int size);

   /**
    * Returns the {@link DataBlockId} of the {@link IDataBlock}.
    *
    * @return the id of the {@link IDataBlock}.
    */
   public DataBlockId getId();

   /**
    * Returns the parent {@link IDataBlock} of this instance. May return null if this is a top-level {@link IDataBlock}.
    *
    * @return the parent of this {@link IDataBlock} or null if this is a top-level {@link IDataBlock}.
    */
   public IDataBlock getParent();

   /**
    * Initially sets the parent {@link IDataBlock} of this {@link IDataBlock}. This method may only be called if a
    * parent is not already set, i.e. if #getParent() returns null.
    *
    * @param parent
    *           The parent to set.
    *
    * @pre {@link #getParent()} == null
    */
   public void initParent(IDataBlock parent);

   /**
    * @param bytes
    */
   public void setBytes(byte[][] bytes);

   /**
    * Frees any internal data associated with this {@link IDataBlock}. Should be called when you use large portions of
    * data or a lot of data blocks over a longer period of time to avoid out-of-memory conditions. Whenever an
    * {@link IDataBlock} is not used anymore, call this method. However, after calling this method, the
    * {@link IDataBlock} instance may still be used.
    */
   public void free();
}
