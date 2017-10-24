/**
 *
 * {@link InputStreamMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * {@link InputStreamMedium} represents an {@link Medium} working on an {@link InputStream}. By definition, an
 * {@link InputStreamMedium} is read-only and non-random-access.
 * 
 * Furthermore, you should only use an {@link InputStreamMedium} in situations where it is necessary, e.g. for accessing
 * real streamed content (e.g. internet radio streams) or zipped content. If performance is crucial and you require true
 * random access, prefer using a {@link FileMedium} or an {@link InMemoryMedium}. For instance, you should prefer using
 * a {@link FileMedium} instead of using a {@link FileInputStream}. If you need to use a stream, be sure to read it
 * mostly sequential and be sure to configure a reasonable maximum cache size for it to avoid out-of-memory situations
 * for "endless" streams.
 * 
 * The reason behind is that the reading implementation uses caching to simulate the possibility of random-access for
 * users of the component, still allowing flexibility to use arbitrary {@link InputStream}s as media.
 */
public class InputStreamMedium extends AbstractMedium<InputStream> {

   /**
    * Creates a new {@link InputStreamMedium} with default values for all properties that influence reading and writing.
    * 
    * @param medium
    *           The {@link InputStream} to use, must not be null
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, null may be passed
    */
   public InputStreamMedium(InputStream medium, String name) {
      this(medium, name, DEFAULT_CACHING_ENABLED, DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
         DEFAULT_MAX_CACHE_REGION_SIZE_IN_BYTES, DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES);
   }

   /**
    * Creates a new {@link InputStreamMedium} and allows to explicitly set all configuration properties that influence
    * reading and writing.
    * 
    * @param medium
    *           The {@link InputStream} to use, must not be null
    * @param name
    *           A name of the {@link InputStream} to be able to identify it. Optional, null may be passed
    * @param cachingEnabled
    *           see {@link #isCachingEnabled()}
    * @param maxCacheSizeInBytes
    *           see #getMaxCacheSizeInBytes()
    * @param maxCacheRegionSizeInBytes
    *           see #getMaxCacheRegionSizeInBytes()
    * @param maxReadWriteBlockSizeInBytes
    *           see #getMaxReadWriteBlockSizeInBytes()
    */
   public InputStreamMedium(InputStream medium, String name, boolean cachingEnabled, long maxCacheSizeInBytes,
      int maxCacheRegionSizeInBytes, int maxReadWriteBlockSizeInBytes) {
      super(medium, name, false, true, cachingEnabled, maxCacheSizeInBytes, maxCacheRegionSizeInBytes,
         maxReadWriteBlockSizeInBytes);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {
      return UNKNOWN_LENGTH;
   }
}
