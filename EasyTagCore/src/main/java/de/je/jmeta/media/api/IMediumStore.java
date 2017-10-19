/**
 *
 * {@link IMediumStore}.java
 *
 * @author Jens Ebert
 *
 * @date 16.10.2017
 *
 */
package de.je.jmeta.media.api;

import java.nio.ByteBuffer;

import de.je.jmeta.media.api.datatype.MediumAction;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.InvalidMediumReferenceException;
import de.je.jmeta.media.api.exception.InvalidOverlappingWriteException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.api.exception.MediumStoreClosedException;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;

/**
 * {@link IMediumStore}
 *
 */
public interface IMediumStore {
   // TODO: DES 048: Also lock medium if it is a read-only medium!
   // TODO: DES 063: Explicitly describe performance drawbacks of inputstream media!
   // TODO: DES 073: Add an "isCacheable" method to IMedium
   // TODO: DES 065: Byte array media are never cached
   // TODO: truncation during writing of data must also clear the corresponding bytes from the cache!

   /**
    * Tells whether this {@link IMediumStore} is opened (true) or already closed (false). On a closed
    * {@link IMediumStore}, all access methods cannot be used anymore and will throw a
    * {@link MediumStoreClosedException}.
    * 
    * @return whether this {@link IMediumStore} is opened (true) or already closed (false)
    */
   public boolean isOpened();

   /**
    * Closes this {@link IMediumStore} and frees any internally held resources such as cached data. Once an
    * {@link IMediumStore} is closed, most of its methods cannot be used anymore and will throw a
    * {@link MediumStoreClosedException}.
    * 
    * @throws MediumAccessException
    *            in case of any problems to perform this operation
    * 
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public void close();

   /**
    * @return the {@link IMedium} this {@link IMediumStore} is working on.
    */
   public IMedium<?> getMedium();

   /**
    * Tells whether the given {@link IMediumReference} points to the end of the given medium or not. For stream-based
    * media, the {@link IMediumReference} is ignored and it is checked if - when currently read would start - the stream
    * is now at its end or not.
    * 
    * @param offset
    *           The {@link IMediumReference} offset to check, ignored for stream-based media. Must not be null and must
    *           point to the same {@link IMedium} as this {@link IMediumStore}
    * @return whether the given {@link IMediumReference} points to the end of the given medium or not
    * 
    * @throws MediumAccessException
    *            in case of any problems to perform this operation
    * 
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public boolean isAtEndOfMedium(IMediumReference offset);

   /**
    * Creates an {@link IMediumReference} for the specified offset. The {@link IMediumReference}s created are internally
    * managed in a pool to ensure proper updates in case of writing changes to the {@link IMedium}. That means, as long
    * as this {@link IMediumStore} is open, all {@link IMediumReference}s previously created with this method are
    * automatically update once changes are written to the underlying medium using this {@link IMediumStore}'s
    * {@link #flush()} method.
    * 
    * @param offset
    *           The offset for which to create the {@link IMediumReference} relative to the {@link IMedium}s starting
    *           point, which is offset 0, must be equal to or bigger than 0
    * @return The {@link IMediumReference} pointing to the {@link IMediumStore}s medium and the given offset.
    * 
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public IMediumReference createMediumReference(long offset);

   /**
    * Caches up to numberOfBytes bytes read from the external {@link IMedium}, starting at the given offset, without
    * returning the data. If data is already cached in the provided range, the method returns without doing anything. It
    * also immediately returns without any action if caching is currently disabled, specifically for non-cacheable
    * media. This method might encounter the end of the {@link IMedium} during reading, in which case it throws an
    * {@link EndOfMediumException}. Calling this method might lead to an internal auto-cleanup of the cache if the
    * maximum configured cache size is exceeded. In this case, the data cached with the very first call to
    * {@link #cache(IMediumReference, int)} since opening the {@link IMedium} is removed from the cache first, then the
    * data of the second call and so on, until the current cache size plus the number of bytes to newly cache is again
    * below the maximum cache size. In an extreme case, the number of bytes to cache passed to this method is itself
    * already exceeding the maximum cache size, which adds only the cache regions with higher offsets to the cache. For
    * example, if numberOfBytes = maxCacheSize + n, after calling this method, only bytes starting from offset + n until
    * offset + n + maxCacheSize are actually cached such that the maximum cache size is not exceeded. To avoid this
    * strange situation, code using this method should ensure a proper configuration of maximum cache size and to pass
    * sizes that are much smaller than the maximum cache size as parameter to this method.
    * 
    * For non-random-access media, this method has a specialized behavior: If passing the already described initial
    * checks (data is not yet in the cache and caching is enabled), it compares the given offset with the last position
    * read from the stream. If the given offset is smaller then the highest previous read position, it throws an
    * {@link InvalidMediumReferenceException}, indicating that you cannot cache bytes from earlier stream positions,
    * because streams cannot look back. If it is equal, it simply reads the given number of bytes. If it is bigger, it
    * reads all bytes until the given offset and possibly caches them (if caching is enabled), and then it tries to read
    * the indicated number of bytes. Of course, during these reads, also an {@link EndOfMediumException} might occur.
    * 
    * This method offers users the possibility to buffer data as soon as its required size is known, thus minimizing
    * explicit read calls to the external medium. The buffered data can then later be fetched using
    * {@link #getData(IMediumReference, int)}. As an alternative, using code can directly call
    * {@link #getData(IMediumReference, int)}, if it is suitable to work with all the read data right away.
    * 
    * @param offset
    *           The offset to start caching, must not be beyond the current medium length, must point to the same
    *           {@link IMedium} as this {@link IMediumStore}
    * @param numberOfBytes
    *           The number of bytes to cache, must be bigger than zero; caching too much bytes might lead to an
    *           {@link OutOfMemoryError}, so users should ensure to configure a maximum cache size if they need to call
    *           this method with vast numbers of bytes
    * 
    * @throws EndOfMediumException
    *            If the method encounters the end of the medium before reading all bytes. The method
    *            {@link EndOfMediumException#getByteCountTriedToRead()} returns the value of the size parameter, the
    *            {@link EndOfMediumException#getMediumReference()} method returns the value of the offset parameter and
    *            the {@link EndOfMediumException#getBytesReallyRead()} method returns the actual number of bytes read up
    *            to the end of medium.
    * @throws InvalidMediumReferenceException
    *            Only for stream media, if the given offset is before the highest previously read offset
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public void cache(IMediumReference offset, int numberOfBytes) throws EndOfMediumException;

   /**
    * Returns the number of consecutively cached bytes at the given offset, or 0 if there are no cached bytes at this
    * offset. Returns 0 if caching is disabled.
    * 
    * @param offset
    *           The offset to use, must point to the same {@link IMedium} as this {@link IMediumStore}
    * @return the number of consecutively cached bytes at the given offset, or 0 if there are no cached bytes at this
    *         offset.
    * 
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public long getCachedByteCountAt(IMediumReference offset);

   /**
    * Returns medium data for the given range. All parts of this range that are found in the internal cache are taken
    * from there, for all other parts, this method reads data directly from the external medium and adds it to the
    * cache. During this, an {@link EndOfMediumException} might occur. Depending on what you previously did, you should
    * handle such an exception in the following ways:
    * <ol>
    * <li>If you called {@link #cache(IMediumReference, int)} before successfully for the same range, an
    * {@link EndOfMediumException} when calling {@link #getData(IMediumReference, int)} with this range is unexpected.
    * This might only happen if the medium was changed by another process meanwhile. You should throw a runtime
    * exception in this case, wrapping the original exception.</li>
    * <li>Otherwise, you should re-call {@link #getData(IMediumReference, int)} using
    * {@link EndOfMediumException#getBytesReallyRead()} as the new number of bytes to read.</li>
    * </ol>
    * 
    * The returned data is provided in a read-only {@link ByteBuffer} between its position and limit, i.e.
    * {@link ByteBuffer#remaining()} equals the read byte count, which equals the input parameter numberOfBytes.
    * 
    * As already indicated, one model of working with this API is to call {@link #cache(IMediumReference, int)} as soon
    * as you know the number of bytes you need to work with ahead, then call {@link #getData(IMediumReference, int)}
    * portion-wise for the same range at places where you need the actually work with the data. ANother option is to not
    * use {@link #cache(IMediumReference, int)} and directly call {@link #getData(IMediumReference, int)} if your code
    * structure allows to directly work with the data. In this case you should re-read with fewer bytes if an
    * {@link EndOfMediumException} occurs.
    * 
    * For random-access media, this method does not throw an exception if it finds that the data is not cached. For
    * stream-based media, if parts of the range are not found in the cache, and the range is before the current highest
    * read offset, an {@link InvalidMediumReferenceException} is thrown, as with streams, you cannot go back.
    * 
    * @param offset
    *           The offset to use, must point to the same {@link IMedium} as this {@link IMediumStore}, must not exceed
    *           the {@link IMedium}'s length as returned by {@link IMedium#getCurrentLength()}.
    * @param numberOfBytes
    *           The number of bytes to read, must be bigger than 0. If the {@link IMedium} is shorter than this range,
    *           an {@link EndOfMediumException} is thrown; getting too much bytes might lead to an
    *           {@link OutOfMemoryError}, so users should at least ensure to configure a maximum cache size if they need
    *           to call this method with vast numbers of bytes
    * @return A read-only {@link ByteBuffer} containing the read bytes between its limit and position
    * 
    * @throws EndOfMediumException
    *            If the method encounters the end of the medium before reading all bytes. To be handles as described
    *            above in the method description. The method {@link EndOfMediumException#getByteCountTriedToRead()}
    *            returns the value of the size parameter, the {@link EndOfMediumException#getMediumReference()} method
    *            returns the value of the offset parameter and the {@link EndOfMediumException#getBytesReallyRead()}
    *            method returns the actual number of bytes read up to the end of medium.
    * @throws InvalidMediumReferenceException
    *            Only for stream media, if the given offset is before the highest previously read offset, and the data
    *            was not found to be cached
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    */
   public ByteBuffer getData(IMediumReference offset, int numberOfBytes);

   /**
    * Inserts the given bytes at the given {@link IMediumReference} offset. This method does not actually write data to
    * the underlying external medium. To trigger explicit writing of the changes, you must explicitly call
    * {@link #flush()} after calling this method. Therefore a call to this method does not change the length of the
    * medium as returned by {@link IMedium#getCurrentLength()} , and further calls to this method MUST NOT adjust their
    * {@link IMediumReference}s according to already posted calls to {@link #removeData(IMediumReference, int)},
    * {@link #insertData(IMediumReference, ByteBuffer)} or {@link #replaceData(IMediumReference, int, ByteBuffer)}. All
    * {@link IMediumReference}s refer to the current length and content of the {@link IMedium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * You can call this method multiple times with exactly the same {@link IMediumReference}. This corresponds to
    * multiple consecutive inserts, i.e. first the data of the first call with length <t>len</t> is inserted at the
    * <t>offset</t>, then the data of the second call is inserted at <t>offset+len</t> and so on. Inserting at the same
    * offset therefore does <i>not</i> mean overwriting any data or changing previous inserts.
    * 
    * If data before the given offset is removed, replaced or inserted by prior calls to the corresponding methods, the
    * actual insert position is shifted correspondingly.
    * 
    * @param offset
    *           The {@link IMediumReference} at which to insert the data. Must point to the {@link IMedium} this
    *           {@link IMediumStore} works on. Must not exceed the {@link IMedium}'s length as returned by
    *           {@link IMedium#getCurrentLength()}.
    * @param dataToInsert
    *           The bytes to insert at the given offset. Only the remaining bytes in the given {@link ByteBuffer} are
    *           inserted, i.e. the bytes between the {@link ByteBuffer}'s position and limit.
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    * @throws ReadOnlyMediumException
    *            If the underlying {@link IMedium} is read-only
    */
   public MediumAction insertData(IMediumReference offset, ByteBuffer dataToInsert);

   /**
    * Removes the given number of bytes at the given {@link IMediumReference} offset. This method does not actually
    * write data to the underlying external medium. To trigger explicit writing of the changes, you must explicitly call
    * {@link #flush()} after calling this method. Therefore a call to this method does not change the length of the
    * medium as returned by {@link IMedium#getCurrentLength()}, and further calls to this method MUST NOT adjust their
    * {@link IMediumReference}s according to already posted calls to {@link #removeData(IMediumReference, int)},
    * {@link #insertData(IMediumReference, ByteBuffer)} or {@link #replaceData(IMediumReference, int, ByteBuffer)}. All
    * {@link IMediumReference}s refer to the current length and content of the {@link IMedium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * Subsequent calls to {@link #removeData(IMediumReference, int)} that affect the same medium region are only
    * supported if the second call fully encloses the first call's region. This is necessary e.g. to first delete a
    * child object, but later also deleting the enclosing parent object.
    * 
    * Other subsequent calls to {@link #removeData(IMediumReference, int)} or
    * {@link #replaceData(IMediumReference, int, ByteBuffer)} referring to overlapping regions are not allowed and lead
    * to an {@link InvalidOverlappingWriteException}.
    * 
    * @param offset
    *           The {@link IMediumReference} at which to remove the data. Must point to the {@link IMedium} this
    *           {@link IMediumStore} works on. Must not exceed the {@link IMedium}'s length as returned by
    *           {@link IMedium#getCurrentLength()}.
    * @param numberOfBytesToRemove
    *           The number of bytes to remove at the given {@link IMediumReference}. Must be bigger than 0.
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    * @throws ReadOnlyMediumException
    *            If the underlying {@link IMedium} is read-only
    * @throws InvalidOverlappingWriteException
    *            If there was already another call to {@link #removeData(IMediumReference, int)} or
    *            {@link #replaceData(IMediumReference, int, ByteBuffer)}, and the current call's range overlaps with the
    *            ranges passed to these previous calls, excluding the case that the new call fully encloses a range
    *            passed to a previous call of {@link #removeData(IMediumReference, int)}
    */
   public MediumAction removeData(IMediumReference offset, int numberOfBytesToRemove);

   /**
    * Replaces the given number of bytes at the given {@link IMediumReference} offset with the given {@link ByteBuffer}
    * contents, potentially shortening or extending existing {@link IMedium} length. This method does not actually write
    * data to the underlying external medium. To trigger explicit writing of the changes, you must explicitly call
    * {@link #flush()} after calling this method. Therefore a call to this method does not change the length of the
    * medium as returned by {@link IMedium#getCurrentLength()}, and further calls to this method MUST NOT adjust their
    * {@link IMediumReference}s according to already posted calls to {@link #removeData(IMediumReference, int)},
    * {@link #insertData(IMediumReference, ByteBuffer)} or {@link #replaceData(IMediumReference, int, ByteBuffer)}. All
    * {@link IMediumReference}s refer to the current length and content of the {@link IMedium}, irrespective of any
    * removals, insertions or replacements that are not yet flushed.
    * 
    * Subsequent calls to {@link #replaceData(IMediumReference, int, ByteBuffer)} that affect the same medium region are
    * only supported if the second call fully encloses the first call's region. This is necessary e.g. to first replace
    * a child object, but later also replacing the enclosing parent object.
    * 
    * Other subsequent calls to {@link #removeData(IMediumReference, int)} or
    * {@link #replaceData(IMediumReference, int, ByteBuffer)} referring to overlapping regions are not allowed and lead
    * to an {@link InvalidOverlappingWriteException}.
    * 
    * @param offset
    *           The {@link IMediumReference} at which to replace the data. Must point to the {@link IMedium} this
    *           {@link IMediumStore} works on. Must not exceed the {@link IMedium}'s length as returned by
    *           {@link IMedium#getCurrentLength()}.
    * @param numberOfBytesToReplace
    *           The number of bytes to replace at the given {@link IMediumReference}. Must be bigger than 0.
    * @param replacementData
    *           The bytes to use as replacement bytes
    * @return A {@link MediumAction} describing the change. Can be used for undoing the change using
    *         {@link #undo(MediumAction)}.
    * 
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    * @throws ReadOnlyMediumException
    *            If the underlying {@link IMedium} is read-only
    * @throws InvalidOverlappingWriteException
    *            If there was already another call to {@link #removeData(IMediumReference, int)} or
    *            {@link #replaceData(IMediumReference, int, ByteBuffer)}, and the current call's range overlaps with the
    *            ranges passed to these previous calls, excluding the case that the new call fully encloses a range
    *            passed to a previous call of {@link #replaceData(IMediumReference, int, ByteBuffer)}
    */
   public MediumAction replaceData(IMediumReference offset, int numberOfBytesToReplace, ByteBuffer replacementData);

   /**
    * Undoes changes made using {@link #insertData(IMediumReference, ByteBuffer)},
    * {@link #removeData(IMediumReference, int)} or {@link #replaceData(IMediumReference, int, ByteBuffer)}. After this
    * method is called, the corresponding change is invalidated, such that a call to {@link #flush()} will not perform
    * this change. Furthermore, {@link MediumAction#isPending()} for the passed {@link MediumAction} instance will
    * return false afterwards.
    * 
    * @param mediumAction
    *           The {@link MediumAction} to undo, must be still pending; must refer to the same {@link IMedium} as this
    *           {@link IMediumStore}
    */
   public void undo(MediumAction mediumAction);

   /**
    * Actually writes all changes since the last flush or since opening this {@link IMediumStore} to the external
    * {@link IMedium}, if any changes are present at all. Changes might only be done using
    * {@link #insertData(IMediumReference, ByteBuffer)}, {@link #removeData(IMediumReference, int)} or
    * {@link #replaceData(IMediumReference, int, ByteBuffer)}. Theses changes can be undone before a flush by using
    * {@link #undo(MediumAction)}, such that the next call to {@link #flush()} does not consider these changes anymore.
    * 
    * After a flush, the {@link MediumAction} instances returned by these methods instances will not be pending anymore,
    * i.e. {@link MediumAction#isPending()} will return false.
    * 
    * Furthermore, after flushing, any {@link IMediumReference}s previously created using
    * {@link #createMediumReference(long)} might have been automatically updated according to the flushed changes. For
    * example, if an {@link #insertData(IMediumReference, ByteBuffer)} of 10 bytes was done at offset x, an
    * {@link IMediumReference} previously created for offset x+1 will have offset x+1+10 after the {@link #flush()} of
    * this insert. The same happens for any replaces or removes at offsets before the {@link IMediumReference}'s offset.
    * {@link IMediumReference}s with offsets before any changes are not changed after a {@link #flush()} of these
    * changes.
    * 
    * @throws MediumAccessException
    *            If any other errors occurred during accessing the medium
    * @throws MediumStoreClosedException
    *            in case this {@link IMediumStore} has already been closed
    * @throws ReadOnlyMediumException
    *            If the underlying {@link IMedium} is read-only
    */
   public void flush();
}
