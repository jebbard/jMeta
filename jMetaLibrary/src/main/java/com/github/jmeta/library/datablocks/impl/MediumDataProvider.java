/**
 *
 * {@link MediumDataProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 19.02.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.FieldDataProvider;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumDataProvider} is a utility class providing some convenience method for controlled access to the medium
 * via a {@link MediumStore}. The main purpose is to ensure that cache fragmentation is low, especially by implementing
 * the method {@link #bufferBeforeRead(MediumOffset, long)} which should be called before actually getting data using
 * {@link #getData(MediumOffset, int)}.
 *
 * This class also provides a default {@link FieldTerminationFinder}.
 */
public class MediumDataProvider {

   private static final Logger LOGGER = LoggerFactory.getLogger(MediumDataProvider.class);

   private final MediumStore mediumStore;

   private class DefaultFieldDataProvider implements FieldDataProvider {

      private MediumOffset currentOffset;

      /**
       * Creates a new {@link DefaultFieldDataProvider}.
       *
       * @param startOffset
       *           The start offset
       */
      public DefaultFieldDataProvider(MediumOffset startOffset) {
         Reject.ifNull(startOffset, "startOffset");
         currentOffset = startOffset;
      }

      /**
       * @see com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.FieldDataProvider#nextData(int)
       */
      @Override
      public ByteBuffer nextData(int byteCount) {

         if (byteCount == 0) {
            return ByteBuffer.allocate(0);
         }

         long cachedAt = mediumStore.getCachedByteCountAt(currentOffset);

         if (cachedAt < byteCount && cachedAt > 0) {
            byteCount = (int) cachedAt;
         } else {
            try {
               mediumStore.cache(currentOffset, byteCount);
            } catch (EndOfMediumException e) {
               byteCount = e.getByteCountActuallyRead();
            }
         }

         ByteBuffer readData = getData(currentOffset, byteCount);

         currentOffset = currentOffset.advance(byteCount);

         return readData;
      }
   }

   /**
    * Creates a new {@link MediumDataProvider}.
    *
    * @param mediumStore
    *           The {@link MediumStore} to use, must not be null
    */
   public MediumDataProvider(MediumStore mediumStore) {
      Reject.ifNull(mediumStore, "mediumStore");
      this.mediumStore = mediumStore;
   }

   /**
    * Returns the underlying {@link MediumStore}.
    *
    * @return the underlying {@link MediumStore}
    */
   public MediumStore getMediumStore() {
      return mediumStore;
   }

   /**
    * Tries to buffer data starting at the given start {@link MediumOffset} by using the medium caching provided by
    * {@link MediumStore}. The main purpose of this method is to ensure a sensible buffering that does not lead to too
    * much cache fragmentation.
    *
    * The given size is just used as rough indicator as follows:
    * <ul>
    * <li>size is allowed to be specified as {@link DataBlockDescription#UNDEFINED}. If so, caching only happens if
    * there are currently exactly 0 bytes cached at the given start {@link MediumOffset}. In that case, exactly
    * {@link Medium#getMaxReadWriteBlockSizeInBytes()} are cached.</li>
    * <li>Otherwise if size is bigger than {@link Medium#getMaxReadWriteBlockSizeInBytes()}, then only exactly
    * {@link Medium#getMaxReadWriteBlockSizeInBytes()} are cached just once.</li>
    * <li>Otherwise if less than size bytes are currently cached at the given offset, this method caches
    * {@link Medium#getMaxReadWriteBlockSizeInBytes()} starting from the first offset that is not already cached</li>
    * </ul>
    *
    * In case of reaching the end of medium during caching, this incident is just logged and otherwise ignored.
    *
    * @param startOffset
    *           The start {@link MediumOffset} for starting buffering
    * @param size
    *           The size indicator for buffering, might be {@link DataBlockDescription#UNDEFINED} to buffer
    *           {@link Medium#getMaxReadWriteBlockSizeInBytes()} bytes if none are buffered at the offset yet
    */
   public void bufferBeforeRead(MediumOffset startOffset, long size) {
      MediumOffset cacheOffset = null;
      int cacheSize = startOffset.getMedium().getMaxReadWriteBlockSizeInBytes();

      long cachedByteCountAt = mediumStore.getCachedByteCountAt(startOffset);

      if (size != DataBlockDescription.UNDEFINED && cachedByteCountAt < size) {
         cacheOffset = startOffset.advance(cachedByteCountAt);
      } else if (size == DataBlockDescription.UNDEFINED && cachedByteCountAt == 0) {
         cacheOffset = startOffset;
      }

      if (cacheOffset != null) {
         try {
            mediumStore.cache(cacheOffset, cacheSize);
         } catch (EndOfMediumException e) {
            // This is not necessarily an error condition, and for buffering it is safe to ignore this
            LOGGER.debug("Reached end of medium during buffering", e);
         }
      }
   }

   /**
    * Retrieves data from the medium starting at the given offset with the given size. Assumes that the number of bytes
    * is actually present and throws a runtime exception in case of an end of medium encountered.
    *
    * See {@link MediumStore#getData(MediumOffset, int)} for more details.
    *
    * @param startOffset
    *           The start offset
    * @param size
    *           The number of bytes to fetch
    * @return A {@link ByteBuffer} according to {@link MediumStore#getData(MediumOffset, int)}
    */
   public ByteBuffer getData(MediumOffset startOffset, int size) {
      Reject.ifNull(startOffset, "reference");

      try {
         return mediumStore.getData(startOffset, size);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Creates a new instance of a {@link FieldDataProvider} able to provide subsequent byte chunks from the medium
    * starting at the given start offset.
    *
    * @param startOffset
    *           The start {@link MediumOffset}
    * @return A new {@link FieldDataProvider} instance
    */
   public FieldDataProvider createFieldDataProvider(MediumOffset startOffset) {
      return new DefaultFieldDataProvider(startOffset);
   }
}
