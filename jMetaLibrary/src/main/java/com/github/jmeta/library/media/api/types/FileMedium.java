/**
 *
 * {@link FileMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FileMedium} represents a file that is both readable and writable.
 */
public class FileMedium extends AbstractMedium<Path> {

   /**
    * Creates a new {@link FileMedium} with default values for all properties that influence reading and writing.
    * 
    * @param medium
    *           see {@link #FileMedium(Path, boolean, boolean, long, int, int)}
    * @param isReadOnly
    *           see {@link #FileMedium(Path, boolean, boolean, long, int, int)}
    */
   public FileMedium(Path medium, boolean isReadOnly) {
      this(medium, isReadOnly, true, DEFAULT_MAX_CACHE_SIZE_IN_BYTES, DEFAULT_MAX_CACHE_REGION_SIZE_IN_BYTES,
         DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES);
   }

   /**
    * Creates a new {@link FileMedium} and allows to explicitly set all configuration properties that influence reading
    * and writing.
    * 
    * @param medium
    *           The {@link Path} to use, must not be null, must be a file and must exist
    * @param isReadOnly
    *           true to make this {@link FileMedium} read-only, false enables read and write.
    * @param cachingEnabled
    *           see {@link #isCachingEnabled()}
    * @param maxCacheSizeInBytes
    *           see #getMaxCacheSizeInBytes()
    * @param maxCacheRegionSizeInBytes
    *           see #getMaxCacheRegionSizeInBytes()
    * @param maxReadWriteBlockSizeInBytes
    *           see #getMaxReadWriteBlockSizeInBytes()
    */
   public FileMedium(Path medium, boolean isReadOnly, boolean cachingEnabled, long maxCacheSizeInBytes,
      int maxCacheRegionSizeInBytes, int maxReadWriteBlockSizeInBytes) {
      super(medium, medium.toAbsolutePath().toString(), true, isReadOnly, cachingEnabled, maxCacheSizeInBytes,
         maxCacheRegionSizeInBytes, maxReadWriteBlockSizeInBytes);

      Reject.ifFalse(Files.isRegularFile(medium), "Files.isRegularFile(medium)");
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      try {
         return Files.size(getWrappedMedium());
      } catch (IOException e) {
         return Medium.UNKNOWN_LENGTH;
      }
   }
}
