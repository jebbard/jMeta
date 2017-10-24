/**
 *
 * {@link Medium}.java
 *
 * @author Jens Ebert
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link Medium} represents a source of binary data storage. It has several different properties:
 * <ul>
 * <li>It is either a random-access medium, or a stream-based (non-random-access) medium</li>
 * <li>A medium is either read-only or read-write, stream-based are always read-only</li>
 * <li>Random-access media have a current length in bytes, stream-based media have {@link #UNKNOWN_LENGTH}</li>
 * <li>Has a human-readable name</li>
 * <li>Has a wrapped underlying {@link Medium} object</li>
 * </ul>
 * 
 * In addition, there are several configuration values that can be used to configure the {@link Medium} for later
 * access by an {@link MediumStore}. These need to be set in the corresponding implementation classes. This interface
 * only offers methods for reading them: See {@link #getMaxReadWriteBlockSizeInBytes()},
 * {@link #getMaxCacheRegionSizeInBytes()}, {@link #getMaxCacheSizeInBytes()} and {@link #isCacheable()}.
 * 
 * @param <T>
 *           The concrete type of wrapped medium object.
 */
public interface Medium<T> {

   /**
    * Refers to an unknown length of this {@link Medium}.
    */
   public static final long UNKNOWN_LENGTH = -1;

   /**
    * The default maximum read-write block size in bytes, if no values are explicitly set. See
    * {@link #getMaxReadWriteBlockSizeInBytes()} for more details.
    */
   public static int DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES = 8192;

   /**
    * The default maximum cache region size in bytes, if no values are explicitly set. See
    * {@link #getMaxCacheRegionSizeInBytes()} for more details.
    */
   public static int DEFAULT_MAX_CACHE_REGION_SIZE_IN_BYTES = DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES;

   /**
    * The default maximum cache size in bytes, if no values are explicitly set. See {@link #getMaxCacheSizeInBytes()}
    * for more details.
    */
   public static long DEFAULT_MAX_CACHE_SIZE_IN_BYTES = 1_048_576L;

   /**
    * The default value for caching enabled, if no values are explicitly set. See {@link #isCachingEnabled()} for more
    * details.
    */
   public static boolean DEFAULT_CACHING_ENABLED = true;

   /**
    * Returns whether this {@link Medium} supports random-access or not.
    * 
    * @return whether this {@link Medium} supports random-access or not.
    */
   public boolean isRandomAccess();

   /**
    * Returns whether this {@link Medium} supports only read-access or not.
    * 
    * @return whether this {@link Medium} supports only read-access or not.
    */
   public boolean isReadOnly();

   /**
    * @return the current length of this {@link Medium}, if it is a random-access medium, or {@link #UNKNOWN_LENGTH} if
    *         it is not. Also returns {@link #UNKNOWN_LENGTH} if the medium does not exist or its length cannot be
    *         determined due to any other reason.
    */
   public long getCurrentLength();

   /**
    * Returns the name of the {@link Medium}, if any, or null.
    * 
    * @return the name of the {@link Medium}, if any, or null.
    */
   public String getName();

   /**
    * Returns the wrapped medium object.
    * 
    * @return the wrapped medium object.
    */
   public T getWrappedMedium();

   /**
    * Returns the currently configured maximum read-write block size. The read-write block size is used during flushing
    * of any writing actions to the {@link Medium} and influences how much bytes at max are read and written at once
    * during a flush. This configuration must only be changed before accessing the {@link Medium} using an
    * {@link MediumStore} the first time. Changes to it after opening an {@link MediumStore} are ignored.
    * 
    * The default value if it is not explicitly set is {@link #DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES}.
    * 
    * @return the currently configured maximum read-write block size
    */
   public int getMaxReadWriteBlockSizeInBytes();

   /**
    * Returns the currently configured maximum cache region size in bytes. If caching is enabled, this value is used
    * when data read from the {@link Medium} is stored in an internal cache by an {@link MediumStore}. The cache
    * consists of regions which must not exceed this maximum size. This size must not exceed the currently configured
    * maximum cache size in bytes. If caching is disabled, this value must not be used or interpreted in any way.
    * 
    * The default value, if caching is enabled and if it is not explicitly set is
    * {@link #DEFAULT_MAX_CACHE_REGION_SIZE_IN_BYTES}.
    * 
    * @return the currently configured maximum cache region size in bytes
    */
   public int getMaxCacheRegionSizeInBytes();

   /**
    * Returns the currently configured maximum cache size in bytes. If caching is enabled, this value is used when data
    * read from the {@link Medium} is stored in an internal cache by an {@link MediumStore}. The cache must not exceed
    * this size, if it does, cached data is automatically freed. In detail, the cached data read the longest time ago is
    * freed first to ensure the new cache size is again below the maximum configured size. This size must be bigger than
    * the currently configured maximum cache region size in bytes. If caching is disabled, this value must not be used
    * or interpreted in any way.
    * 
    * The default value, if caching is enabled and if it is not explicitly set is
    * {@link #DEFAULT_MAX_CACHE_SIZE_IN_BYTES}.
    * 
    * @return the currently configured maximum cache size in bytes
    */
   public long getMaxCacheSizeInBytes();

   /**
    * Returns the current configuration for enabling caching (true) or disabling caching (false) for this
    * {@link Medium}.
    * 
    * The default value, if it is not explicitly set is {@link #DEFAULT_CACHING_ENABLED}.
    * 
    * @return the current configuration for enabling caching (true) or disabling caching (false)
    */
   public boolean isCachingEnabled();

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    * 
    *      Two {@link Medium} implementations are equal if and only if they refer to the same (i.e. an equal) wrapped
    *      medium object.
    */
   public boolean equals(Object obj);

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode();

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString();
}