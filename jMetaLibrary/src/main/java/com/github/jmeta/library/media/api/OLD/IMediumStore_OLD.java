package com.github.jmeta.library.media.api.OLD;

import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.exceptions.ReadTimedOutException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;

/**
 * {@link IMediumStore_OLD} defines primitives to work with a {@link Medium}. The implementation might or might not be
 * backed by a caching mechanism. This is transparent to the caller. Only guarantee is: a call to
 * {@link #buffer(MediumReference, long)} will load data into memory that can later be obtained using
 * {@link #getData(MediumReference, int)}. This memory is called the pre-load store, and could also be referred to as
 * some basic cache already.
 * 
 * Clients should be sure to call {@link #close()} as soon they do not need the {@link IMediumStore_OLD} anymore, thus
 * avoiding memory leakage.
 */
public interface IMediumStore_OLD {

   /**
    * Closes this {@link IMediumStore_OLD}. All access methods cannot be used if the {@link IMediumStore_OLD} is closed.
    * 
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @post #isOpened() == false
    */
   public void close();

   /**
    * Creates a new {@link MediumReference}.
    * 
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link Medium}, relative to its starting point which is offset 0. Must not
    *           be smaller than 0.
    * @return The {@link MediumReference} created
    * 
    * @pre #isOpened() == true
    * @pre absoluteMediumOffset >= 0
    */
   public MediumReference createMediumReference(long absoluteMediumOffset);

   /**
    * Discards pre-loaded data starting at the given {@link MediumReference}, up to the specified number of bytes. The
    * following rules apply to this method:
    * <ul>
    * <li>If the given size is equal to 0, the method returns silently without discarding anything.</li>
    * <li>If the given start {@link MediumReference} is not contained in any of the internal regions, the call is
    * ignored and nothing is discarded.</li>
    * <li>Otherwise if the given {@link MediumReference} is contained in the pre-load store, the method determines the
    * number of consecutive bytes pre-loaded at this {@link MediumReference} and discards up to size bytes. The method
    * might trim an existing region if the requested {@link MediumReference} and size overlap it partly.</li>
    * <li>If there is no consecutive data region of the given size, data is discarded up to the last byte stored
    * consecutively starting at the given {@link MediumReference}.</li>
    * </ul>
    * 
    * @param startReference
    *           The {@link MediumReference} to start discarding pre-loaded data at. Must not exceed the {@link Medium}
    *           's length as returned by {@link Medium#getCurrentLength()}.
    * @param size
    *           The size to discard at maximum.
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre {@link #getMedium()}.equals(startReference.getMedium())
    * @pre size >= 0
    */
   public void discard(MediumReference startReference, long size);

   /**
    * Actually writes changed data to the external {@link Medium}, if any changes are present at all. Changes might
    * only be done using {@link #insertData(MediumReference, ByteBuffer)}, {@link #removeData(MediumReference, int)}
    * or {@link #replaceData(MediumReference, int, ByteBuffer)}.
    * 
    * All {@link MediumAction} instances returned by these instances will be invalid after this call and must not be
    * used anymore.
    * 
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @pre #isOpened() == true
    */
   public void flush();

   /**
    * Returns data from this {@link IMediumStore_OLD}. The data is taken from the internal store, if the
    * {@link MediumReference} up to the given byte count has already been pre-loaded using
    * {@link #buffer(MediumReference, long)} before. Otherwise it is read from the {@link Medium} directly. If the end
    * of the {@link Medium} is hit during reading, a {@link EndOfMediumException} is thrown. However, all bytes already
    * read up to end of medium can still be obtained. It is up to the caller what the correct reaction to this exception
    * is.
    * 
    * The returned {@link ByteBuffer} contains the {@link Medium} bytes between its position and limit, i.e.
    * {@link ByteBuffer#remaining()} equals the read byte count, which equals the input parameter byteCount, unless end
    * of medium has been encountered during read, where it equals the actually read bytes up to end of medium. Other
    * bytes that might be stored in the returned {@link ByteBuffer} before its position or behind its limit must be
    * ignored.
    * 
    * This method does not change the internal pre-load store.
    * 
    * @param reference
    *           The {@link MediumReference} from which to retrieve the data. Must point to the {@link Medium} this
    *           {@link IMediumStore_OLD} works on. Must not exceed the {@link Medium}'s length as returned by
    *           {@link Medium#getCurrentLength()}.
    * @param byteCount
    *           The number of bytes to be retrieved. Must be bigger than 0.
    * @return A {@link ByteBuffer} instance with {@link ByteBuffer#remaining()} equal to the requested byte count and
    *         with all retrieved bytes between its position and limit.
    * @throws EndOfMediumException
    *            If the end of the {@link Medium} has been reached during reading. The {@link ByteBuffer} returned is
    *            guaranteed to hold all the bytes up to end of {@link Medium} when this exception is thrown. The
    *            exception itself contains the number of bytes really read, the {@link ByteBuffer}s remaining bytes
    *            equals that value.
    * @throws ReadTimedOutException
    *            If during reading a configured read timeout (as set by has expired prematurely, i.e. before the full
    *            number of bytes requested has been read. The {@link ByteBuffer} returned is guaranteed to hold all the
    *            bytes read up to expiration of the timeout when this exception is thrown. The exception itself contains
    *            the number of bytes really read, the {@link ByteBuffer}s remaining bytes equals that value.
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre byteCount > 0
    */
   public ByteBuffer getData(MediumReference reference, int byteCount) throws EndOfMediumException;

   /**
    * Returns the {@link Medium} this {@link IMediumStore_OLD} is using.
    * 
    * @return the {@link Medium} this {@link IMediumStore_OLD} is using.
    */
   public Medium<?> getMedium();

   /**
    * Returns the number of bytes currently buffered at the given {@link MediumReference}.
    * 
    * @param reference
    *           The {@link MediumReference}. Must point to the {@link Medium} this {@link IMediumStore_OLD} works on.
    * @return the number of bytes currently buffered at the given {@link MediumReference}. 0 if no bytes are buffered
    *         at that {@link MediumReference}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    */
   public long getBufferedByteCountAt(MediumReference reference);

   /**
    * Inserts the given bytes at the given {@link MediumReference}. This method does not actually write data to the
    * underlying external medium. To trigger explicit writing of the changes, you must explicitly call {@link #flush()}
    * after calling this method. Therefore a call to this method does not change the length of the medium as returned by
    * {@link Medium#getCurrentLength()} , and further calls to this method MUST NOT adjust their
    * {@link MediumReference}s according to already posted calls to {@link #removeData(MediumReference, int)},
    * {@link #insertData(MediumReference, ByteBuffer)} or {@link #replaceData(MediumReference, int, ByteBuffer)}. All
    * {@link MediumReference}s refer to the current length and content of the {@link Medium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * You can call this method multiple times with exactly the same {@link MediumReference}. This corresponds to
    * multiple consecutive inserts, i.e. first the data of the first call with length <t>len</t> is inserted at the
    * <t>offset</t>, then the data of the second call is inserted at <t>offset+len</t> and so on. Inserting at the same
    * offset therefore does <i>not</i> mean overwriting any data.
    * 
    * If data before the given offset is removed or inserted, the actual insert position is shifted correspondingly.
    * 
    * @param reference
    *           The {@link MediumReference} at which to insert the data. Must point to the {@link Medium} this
    *           {@link IMediumStore_OLD} works on. Must not exceed the {@link Medium}'s length as returned by
    *           {@link Medium#getCurrentLength()}.
    * @param bytes
    *           The bytes to insert at the location. Only the remaining bytes in the given {@link ByteBuffer} are
    *           inserted, i.e. the bytes between the {@link ByteBuffer} position and limit.
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    */
   public MediumAction insertData(MediumReference reference, ByteBuffer bytes);

   /**
    * Returns whether the given {@link MediumReference} is at end of the {@link Medium} used for reading. The method
    * may potentially block forever as it might require reading a byte to detect end of medium. There is not timeout
    * option.
    * 
    * @param reference
    *           The {@link MediumReference} to check for end of medium. Must refer to the same {@link Medium} as this
    *           {@link IMediumStore_OLD} uses.
    * 
    * @return Returns whether the given {@link MediumReference} is at end of the {@link Medium}.
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre #isOpened() == true
    */
   public boolean isAtEndOfMedium(MediumReference reference);

   /**
    * Returns whether this {@link IMediumStore_OLD} is currently opened or closed. A newly created instance of an
    * implementing class must be initially open.
    * 
    * @return Returns whether this {@link IMediumStore_OLD} is currently opened (true) or closed (false).
    */
   public boolean isOpened();

   /**
    * Buffers data of the given size from the {@link Medium} starting from the given {@link MediumReference} into the
    * internal pre-load store. The buffering only happens if the data is not yet entirely buffered. All later calls to
    * {@link #getData(MediumReference, int)} that fully refer to already buffered data will read from the pre-load
    * store.
    * 
    * The purpose of buffering an amount of data is to minimize read calls to the external medium, while still being
    * able to process the data at the place it is required.
    * 
    * This method might throw an {@link EndOfMediumException}. The pre-load then stops at the end of the {@link Medium}
    * .
    * 
    * In detail, the buffering is done with the following rules:
    * <ul>
    * <li>If the new region to be buffered is already in the pre-load store in total, the method returns without any
    * changes made to the pre-load store. No data is read from the reading medium. This is of course also true for the
    * special case when this method is called multiple times with the same arguments.</li>
    * <li>Otherwise if the new region incorporates one or several complete existing regions, the existing regions are
    * dropped and the new region is then buffered as such.</li>
    * <li>Otherwise if the new region overlaps the end of an existing region, the existing region remains unchanged, the
    * new region is trimmed to start at the first byte after the existing region and then buffered as such.</li>
    * <li>Otherwise if the new region overlaps the start of an existing region, the existing region remains unchanged,
    * the new region is trimmed to end at the last byte before the existing region and then buffered as such.</li>
    * </ul>
    * 
    * This way, refreshing of the pre-load store is only possible if more data is buffered.
    * 
    * @param reference
    *           The {@link MediumReference} where to start buffering data. Must point to the {@link Medium} this
    *           {@link IMediumStore_OLD} works on. Must not exceed the {@link Medium}'s length as returned by
    *           {@link Medium#getCurrentLength()}.
    * @param size
    *           The size to be cached. Must be bigger than 0. Buffering too much data might lead to an out-of-memory
    *           condition.
    * @throws EndOfMediumException
    *            If during reading from the {@link Medium}, the end of the {@link Medium} is hit before having read
    *            all bytes into the pre-load store. The {@link EndOfMediumException}'s
    *            {@link EndOfMediumException#getByteCountTriedToRead()} method will return the value of the size
    *            parameter, the {@link EndOfMediumException#getMediumReference()} method will return the value of the
    *            reference parameter and the {@link EndOfMediumException#getBytesReallyRead()} method will return the
    *            number of bytes read up to the end of medium.
    * @throws ReadTimedOutException
    *            If during reading from the {@link Medium}, a timeout for reading has expired.
    * @throws MediumAccessException
    *            In case of any errors during accessing the underlying {@link Medium}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre size > 0
    * 
    * @post without EndOfMediumException: {@link #getBufferedByteCountAt(MediumReference)} == true for the given
    *       {@link MediumReference} and size
    * @post with EndOfMediumException: if {@link EndOfMediumException#getBytesReallyRead()} > 0,
    *       {@link #getBufferedByteCountAt(MediumReference)} == true for the given {@link MediumReference} and
    *       {@link EndOfMediumException#getBytesReallyRead()}
    */
   public void buffer(MediumReference reference, int size) throws EndOfMediumException;

   /**
    * Removes the given number of bytes at the given {@link MediumReference}. This method does not actually write data
    * to the underlying external medium. To trigger explicit writing of the changes, you must explicitly call
    * {@link #flush()} after calling this method. Therefore a call to this method does not change the length of the
    * medium as returned by {@link Medium#getCurrentLength()}, and further calls to this method MUST NOT adjust their
    * {@link MediumReference}s according to already posted calls to {@link #removeData(MediumReference, int)},
    * {@link #insertData(MediumReference, ByteBuffer)} or {@link #replaceData(MediumReference, int, ByteBuffer)}. All
    * {@link MediumReference}s refer to the current length and content of the {@link Medium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * Subsequent calls to {@link #removeData(MediumReference, int)} are supported, even if they overlap. They can
    * individually be undone. If this method is called several times with exactly the same input parameters, this is
    * interpreted as single removal of the corresponding region only and therefore can only be undone once.
    * 
    * If multiple subsequent calls to {@link #removeData(MediumReference, int)} and/or
    * {@link #replaceData(MediumReference, int, ByteBuffer)} refer to overlapping regions, at flush time, a byte
    * present in both regions is changed according to the most recent call to one of these methods, i.e. always the last
    * posted change is persisted.
    * 
    * @param reference
    *           The {@link MediumReference} at which to remove the data. Must point to the {@link Medium} this
    *           {@link IMediumStore_OLD} works on. Must not exceed the {@link Medium}'s length as returned by
    *           {@link Medium#getCurrentLength()}.
    * @param byteCountToRemove
    *           The number of bytes to remove at the given {@link MediumReference}. Must be bigger than 0.
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre byteCountToRemove > 0
    */
   public MediumAction removeData(MediumReference reference, int byteCountToRemove);

   /**
    * Replaces the given number of bytes at the given {@link MediumReference} with the given {@link ByteBuffer}
    * contents, potentially shortening or extending existing data length. This method does not actually write data to
    * the underlying external medium. To trigger explicit writing of the changes, you must explicitly call
    * {@link #flush()} after calling this method. Therefore a call to this method does not change the length of the
    * medium as returned by {@link Medium#getCurrentLength()}, and further calls to this method MUST NOT adjust their
    * {@link MediumReference}s according to already posted calls to {@link #removeData(MediumReference, int)},
    * {@link #insertData(MediumReference, ByteBuffer)} or {@link #replaceData(MediumReference, int, ByteBuffer)}. All
    * {@link MediumReference}s refer to the current length and content of the {@link Medium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * Subsequent calls to {@link #replaceData(MediumReference, int, ByteBuffer)} are supported, even if they overlap.
    * They can individually be undone.
    * 
    * If multiple subsequent calls to {@link #removeData(MediumReference, int)} and/or
    * {@link #replaceData(MediumReference, int, ByteBuffer)} refer to overlapping regions, at flush time, a byte
    * present in both regions is changed according to the most recent call to one of these methods, i.e. always the last
    * posted change is persisted.
    * 
    * @param reference
    *           The {@link MediumReference} at which to replace the data. Must point to the {@link Medium} this
    *           {@link IMediumStore_OLD} works on. Must not exceed the {@link Medium}'s length as returned by
    *           {@link Medium#getCurrentLength()}.
    * @param byteCountToReplace
    *           The number of bytes to replace at the given {@link MediumReference}. Must be bigger than 0.
    * @param bytes
    *           The bytes to replace
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @pre #isOpened() == true
    * @pre {@link #getMedium()}.getCurrentLength() > reference.getAbsoluteByteOffset()
    * @pre {@link #getMedium()}.equals(reference.getMedium())
    * @pre byteCountToReplace > 0
    */
   public MediumAction replaceData(MediumReference reference, int byteCountToReplace, ByteBuffer bytes);

   /**
    * Undoes changes made using {@link #insertData(MediumReference, ByteBuffer)},
    * {@link #removeData(MediumReference, int)} or {@link #replaceData(MediumReference, int, ByteBuffer)}.
    * 
    * @param handle
    *           The {@link MediumAction} to undo, must be valid
    * 
    * @pre handle.isValid() == true
    */
   public void undo(MediumAction handle);
}