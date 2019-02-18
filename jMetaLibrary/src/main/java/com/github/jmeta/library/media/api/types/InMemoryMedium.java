/**
 *
 * {@link InMemoryMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * {@link InMemoryMedium} represents data stored already in memory.
 */
public class InMemoryMedium extends AbstractMedium<ByteBuffer> {

   /**
    * Creates a new {@link InMemoryMedium} with default values for all properties that influence reading and writing.
    * Note that caching is always disable for an {@link InMemoryMedium}.
    *
    * @param medium
    *           See {@link #InMemoryMedium(byte[], String, boolean, long, int)}
    * @param name
    *           See {@link #InMemoryMedium(byte[], String, boolean, long, int)}
    * @param readOnly
    *           See {@link #InMemoryMedium(byte[], String, boolean, long, int)}
    */
   public InMemoryMedium(byte[] medium, String name, boolean readOnly) {
      this(medium, name, readOnly, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES);
   }

   /**
    * Creates a new {@link InMemoryMedium} and allows to explicitly set all configuration properties that influence
    * reading and writing. Note that caching is always disable for an {@link InMemoryMedium}.
    *
    * @param medium
    *           The underlying byte array, must not be null
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, i.e. null may be passed
    * @param isReadOnly
    *           true to make this {@link InMemoryMedium} read-only, false enables read and write
    * @param maxCacheSizeInBytes
    *           see {@link Medium#getMaxCacheSizeInBytes()}, must be bigger than 0
    * @param maxReadWriteBlockSizeInBytes
    *           see {@link Medium#getMaxReadWriteBlockSizeInBytes()}
    */
   public InMemoryMedium(byte[] medium, String name, boolean isReadOnly, long maxCacheSizeInBytes,
      int maxReadWriteBlockSizeInBytes) {
      super(ByteBuffer.wrap(medium), name, true, isReadOnly, true, maxCacheSizeInBytes, maxReadWriteBlockSizeInBytes);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      return getWrappedMedium().remaining();
   }

   /**
    * Provides the possibility to reset the current medium bytes.
    *
    * @param mediumBytes
    *           The bytes to set
    */
   public void setBytes(ByteBuffer mediumBytes) {

      setNewMediumContent(mediumBytes);
   }
}
