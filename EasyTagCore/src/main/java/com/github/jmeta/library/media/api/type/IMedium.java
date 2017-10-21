/**
 *
 * {@link IMedium}.java
 *
 * @author Jens Ebert
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.type;

/**
 * {@link IMedium} represents a source of binary data storage.
 * 
 * @param <T>
 *           The concrete type of wrapped medium object.
 */
public interface IMedium<T> {

   /**
    * Refers to an unknown length of this {@link IMedium}.
    */
   public static final long UNKNOWN_LENGTH = -1;

   /**
    * Returns whether this {@link IMedium} supports random-access or not.
    * 
    * @return whether this {@link IMedium} supports random-access or not.
    */
   public boolean isRandomAccess();

   /**
    * Returns whether this {@link IMedium} already exists or not.
    * 
    * @return whether this {@link IMedium} already exists or not.
    */
   public boolean exists();

   /**
    * Returns whether this {@link IMedium} supports only read-access or not.
    * 
    * @return whether this {@link IMedium} supports only read-access or not.
    */
   public boolean isReadOnly();

   /**
    * Returns whether this {@link IMedium} in principle supports caching or not.
    * 
    * @return whether this {@link IMedium} in principle supports caching or not.
    */
   boolean isCacheable();

   /**
    * @return the current length of this {@link IMedium}, if it is a random-access medium, or {@link #UNKNOWN_LENGTH} if
    *         it is not. Also returns {@link #UNKNOWN_LENGTH} if the medium does not exist or its length cannot be
    *         determined due to any other reason.
    */
   public long getCurrentLength();

   /**
    * Returns the name of the {@link IMedium}, if any, or null.
    * 
    * @return the name of the {@link IMedium}, if any, or null.
    */
   public String getName();

   /**
    * Returns the wrapped medium object.
    * 
    * @return the wrapped medium object.
    */
   public T getWrappedMedium();

   // /**
   // * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
   // * bytes read or written during a flush operation.
   // */
   // public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
   // InMemoryMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1,
   // Integer.MAX_VALUE);

   // /**
   // * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
   // * contents.
   // */
   // public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
   // FileMedium.class.getName() + ".ENABLE_CACHING", true);
   //
   // /**
   // * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
   // * cache region size will not have any effect on existing cache regions
   // */
   // public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
   // FileMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
   // * bytes read or written during a flush operation.
   // */
   // public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
   // FileMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
   // * immediately frees up data if the new max cache size is exceeded by current cache contents.
   // */
   // public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
   // FileMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L, Long.MAX_VALUE);

   // /**
   // * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
   // * contents.
   // */
   // public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
   // InputStreamMedium.class.getName() + ".ENABLE_CACHING", true);
   //
   // /**
   // * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
   // * cache region size will not have any effect on existing cache regions
   // */
   // public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
   // InputStreamMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
   // * immediately frees up data if the new max cache size is exceeded by current cache contents.
   // */
   // public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
   // InputStreamMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L, Long.MAX_VALUE);
   //
   // /**
   // * Parameter for enabling that bytes are skipped when reading forward, default is: false. This parameter influences
   // * the behavior of read operations on {@link InputStreamMedium}s. If this parameter is set to true, then any bytes
   // * between the last read offset and the read offset of the next read operation on an {@link InputStreamMedium} (if
   // * any) are skipped instead of being read to the cache.
   // */
   // public final static AbstractConfigParam<Boolean> SKIP_ON_FORWARD_READ = new BooleanConfigParam(
   // InputStreamMedium.class.getName() + ".SKIP_ON_FORWARD_READ", false);

   /**
    * Sets the maximum read-write block size, see {@link #getMaxReadWriteBlockSizeInBytes()} for details.
    * 
    * @param maxReadWriteBlockSizeInBytes
    *           the new maximum read-write block size, must not be negative
    */
   void setMaxReadWriteBlockSizeInBytes(int maxReadWriteBlockSizeInBytes);

   /**
    * Returns the currently configured maximum read-write block size. The read-write block size is used during flushing
    * of any writing actions to the {@link IMedium} and influence how much bytes at max are read and written at once
    * during a flush. This
    * 
    * @return
    */
   int getMaxReadWriteBlockSizeInBytes();

   void setMaxCacheRegionSizeInBytes(int maxCacheRegionSizeInBytes);

   int getMaxCacheRegionSizeInBytes();

   void setMaxCacheSizeInBytes(long maxCacheSizeInBytes);

   long getMaxCacheSizeInBytes();

   void setCachingEnabled(boolean cachingEnabled);

   boolean isCachingEnabled();

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    * 
    *      Two {@link IMedium} implementations are equal if and only if they refer to the same (i.e. an equal) wrapped
    *      medium object.
    */
   @Override
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
   // /**
   // * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
   // * bytes read or written during a flush operation.
   // */
   // public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
   // InMemoryMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1,
   // Integer.MAX_VALUE);

   // /**
   // * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
   // * contents.
   // */
   // public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
   // FileMedium.class.getName() + ".ENABLE_CACHING", true);
   //
   // /**
   // * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
   // * cache region size will not have any effect on existing cache regions
   // */
   // public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
   // FileMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum write block size, default is: 8192 bytes. It determines the maximum number of
   // * bytes read or written during a flush operation.
   // */
   // public final static AbstractConfigParam<Integer> MAX_WRITE_BLOCK_SIZE = new IntegerConfigParam(
   // FileMedium.class.getName() + ".MAX_WRITE_BLOCK_SIZE", 8192, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
   // * immediately frees up data if the new max cache size is exceeded by current cache contents.
   // */
   // public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
   // FileMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L, Long.MAX_VALUE);

   // /**
   // * Parameter for enabling caching, default is: Caching is enabled. Disabling caching will empty current cache
   // * contents.
   // */
   // public final static AbstractConfigParam<Boolean> ENABLE_CACHING = new BooleanConfigParam(
   // InputStreamMedium.class.getName() + ".ENABLE_CACHING", true);
   //
   // /**
   // * Parameter for setting the maximum cache region size, default is: Unlimited cache region size. Changing the max
   // * cache region size will not have any effect on existing cache regions
   // */
   // public final static AbstractConfigParam<Integer> MAX_CACHE_REGION_SIZE = new IntegerConfigParam(
   // InputStreamMedium.class.getName() + ".MAX_CACHE_REGION_SIZE", Integer.MAX_VALUE, 1, Integer.MAX_VALUE);
   //
   // /**
   // * Parameter for setting the maximum cache size, default is: Unlimited cache size. Changing the max cache size
   // * immediately frees up data if the new max cache size is exceeded by current cache contents.
   // */
   // public final static AbstractConfigParam<Long> MAX_CACHE_SIZE = new LongConfigParam(
   // InputStreamMedium.class.getName() + ".MAX_CACHE_SIZE", Long.MAX_VALUE, 1L, Long.MAX_VALUE);
   //
   // /**
   // * Parameter for enabling that bytes are skipped when reading forward, default is: false. This parameter influences
   // * the behavior of read operations on {@link InputStreamMedium}s. If this parameter is set to true, then any bytes
   // * between the last read offset and the read offset of the next read operation on an {@link InputStreamMedium} (if
   // * any) are skipped instead of being read to the cache.
   // */
   // public final static AbstractConfigParam<Boolean> SKIP_ON_FORWARD_READ = new BooleanConfigParam(
   // InputStreamMedium.class.getName() + ".SKIP_ON_FORWARD_READ", false);

}
