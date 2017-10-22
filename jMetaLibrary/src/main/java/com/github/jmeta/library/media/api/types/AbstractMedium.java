/**
 *
 * {@link AbstractMedium}.java
 *
 * @author Jens Ebert
 *
 * @date 24.12.2010
 *
 */

package com.github.jmeta.library.media.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractMedium} represents a physical medium where binary data can be stored.
 * 
 * @param <T>
 *           The concrete type of wrapped medium object.
 */
public abstract class AbstractMedium<T> implements IMedium<T> {

   private T medium;

   private final String name;

   private final boolean isRandomAccess;

   private final boolean isReadOnly;

   private final boolean isCacheable;

   private boolean cachingEnabled = DEFAULT_CACHING_ENABLED;

   private long maxCacheSizeInBytes = DEFAULT_MAX_CACHE_SIZE_IN_BYTES;

   private int maxCacheRegionSizeInBytes = DEFAULT_MAX_CACHE_REGION_SIZE_IN_BYTES;

   private int maxReadWriteBlockSizeInBytes = DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES;

   /**
    * Creates a new {@link AbstractMedium}.
    * 
    * @param medium
    *           The medium to store.
    * @param name
    *           An externally known name of the {@link AbstractMedium} that must be used as medium name. May be null to
    *           indicate that only the physical name of the {@link AbstractMedium} is relevant.
    * @param isRandomAccess
    *           true if the medium is random-access, false otherwise
    * @param isReadOnly
    *           true to make this a read-only {@link IMedium}, false otherwise.
    * @param isCacheable
    *           true if this {@link AbstractMedium} supports caching, false otherwise
    */
   public AbstractMedium(T medium, String name, boolean isRandomAccess, boolean isReadOnly, boolean isCacheable) {
      Reject.ifNull(medium, "medium");

      this.medium = medium;
      this.name = name;
      this.isRandomAccess = isRandomAccess;
      this.isReadOnly = isReadOnly;
      this.isCacheable = isCacheable;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((medium == null) ? 0 : medium.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      AbstractMedium<?> other = (AbstractMedium<?>) obj;
      if (medium == null) {
         if (other.medium != null) {
            return false;
         }
      } else if (!medium.equals(other.medium)) {
         return false;
      }
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "AbstractMedium [medium=" + medium + ", name=" + name + ", isRandomAccess=" + isRandomAccess
         + ", isReadOnly=" + isReadOnly + ", isCacheable=" + isCacheable + ", cachingEnabled=" + cachingEnabled
         + ", maxCacheSizeInBytes=" + maxCacheSizeInBytes + ", maxCacheRegionSizeInBytes=" + maxCacheRegionSizeInBytes
         + ", maxReadWriteBlockSizeInBytes=" + maxReadWriteBlockSizeInBytes + "]";
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#getName()
    */
   @Override
   public String getName() {

      return name;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#getWrappedMedium()
    */
   @Override
   public T getWrappedMedium() {

      return medium;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#isRandomAccess()
    */
   @Override
   public boolean isRandomAccess() {

      return isRandomAccess;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#isReadOnly()
    */
   @Override
   public boolean isReadOnly() {

      return isReadOnly;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#isCacheable()
    */
   @Override
   public boolean isCacheable() {
      return isCacheable;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#isCachingEnabled()
    */
   @Override
   public boolean isCachingEnabled() {
      return cachingEnabled;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#setCachingEnabled(boolean)
    */
   @Override
   public void setCachingEnabled(boolean cachingEnabled) {
      this.cachingEnabled = cachingEnabled;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#getMaxCacheSizeInBytes()
    */
   @Override
   public long getMaxCacheSizeInBytes() {
      return maxCacheSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#setMaxCacheSizeInBytes(long)
    */
   @Override
   public void setMaxCacheSizeInBytes(long maxCacheSizeInBytes) {
      Reject.ifNegativeOrZero(maxCacheSizeInBytes, "maxCacheSizeInBytes");
      Reject.ifTrue(maxCacheSizeInBytes < getMaxCacheRegionSizeInBytes(),
         "maxCacheSizeInBytes < getMaxCacheRegionSizeInBytes()");

      this.maxCacheSizeInBytes = maxCacheSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#getMaxCacheRegionSizeInBytes()
    */
   @Override
   public int getMaxCacheRegionSizeInBytes() {
      return maxCacheRegionSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#setMaxCacheRegionSizeInBytes(int)
    */
   @Override
   public void setMaxCacheRegionSizeInBytes(int maxCacheRegionSizeInBytes) {
      Reject.ifNegativeOrZero(maxCacheRegionSizeInBytes, "maxCacheRegionSizeInBytes");
      Reject.ifTrue(maxCacheRegionSizeInBytes > getMaxCacheSizeInBytes(),
         "maxCacheRegionSizeInBytes > getMaxCacheSizeInBytes()");

      this.maxCacheRegionSizeInBytes = maxCacheRegionSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#getMaxReadWriteBlockSizeInBytes()
    */
   @Override
   public int getMaxReadWriteBlockSizeInBytes() {
      return maxReadWriteBlockSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.IMedium#setMaxReadWriteBlockSizeInBytes(int)
    */
   @Override
   public void setMaxReadWriteBlockSizeInBytes(int maxReadWriteBlockSizeInBytes) {
      Reject.ifNegativeOrZero(maxReadWriteBlockSizeInBytes, "maxReadWriteBlockSizeInBytes");

      this.maxReadWriteBlockSizeInBytes = maxReadWriteBlockSizeInBytes;
   }

   /**
    * Provides the possibility to overwrite the wrapped medium.
    * 
    * @param newMedium
    *           The new medium content.
    */
   protected void setNewMediumContent(T newMedium) {
      Reject.ifNull(newMedium, "medium");

      this.medium = newMedium;
   }
}
