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
public abstract class AbstractMedium<T> implements Medium<T> {

   private final boolean isRandomAccess;

   private final boolean isReadOnly;

   private final long maxCacheSizeInBytes;

   private final int maxReadWriteBlockSizeInBytes;

   private T medium;

   private final String name;

   private final boolean requiresCaching;

   /**
    * Creates a new {@link AbstractMedium} with default values used for all configuration values, see
    * {@link #getMaxCacheSizeInBytes()} and {@link #getMaxReadWriteBlockSizeInBytes()}.
    *
    * @param medium
    *           see {@link #AbstractMedium(Object, String, boolean, boolean, boolean, long, int)}
    * @param name
    *           see {@link #AbstractMedium(Object, String, boolean, boolean, boolean, long, int)}
    * @param isRandomAccess
    *           see {@link #AbstractMedium(Object, String, boolean, boolean, boolean, long, int)}
    * @param isReadOnly
    *           see {@link #AbstractMedium(Object, String, boolean, boolean, boolean, long, int)}
    * @param requiresCaching
    *           see {@link #AbstractMedium(Object, String, boolean, boolean, boolean, long, int)}
    */
   public AbstractMedium(T medium, String name, boolean isRandomAccess, boolean isReadOnly, boolean requiresCaching) {
      this(medium, name, isRandomAccess, isReadOnly, true, DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
         DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES);
   }

   /**
    * Creates a new {@link AbstractMedium}, using explicit values for all properties.
    *
    * @param medium
    *           The underlying raw medium object
    * @param name
    *           An externally known name of the {@link AbstractMedium} that must be used as medium name. May be null to
    *           indicate that only the physical name of the {@link AbstractMedium} is relevant.
    * @param isRandomAccess
    *           true if the medium is random-access, false otherwise
    * @param isReadOnly
    *           true to make this a read-only {@link Medium}, false otherwise.
    * @param requiresCaching
    *           see {@link #requiresCaching()}
    * @param maxCacheSizeInBytes
    *           see {@link #getMaxCacheSizeInBytes()}
    * @param maxReadWriteBlockSizeInBytes
    *           see {@link #getMaxReadWriteBlockSizeInBytes()}
    */
   public AbstractMedium(T medium, String name, boolean isRandomAccess, boolean isReadOnly, boolean requiresCaching,
      long maxCacheSizeInBytes, int maxReadWriteBlockSizeInBytes) {
      Reject.ifNegativeOrZero(maxReadWriteBlockSizeInBytes, "maxReadWriteBlockSizeInBytes");
      Reject.ifNegative(maxCacheSizeInBytes, "maxCacheSizeInBytes");
      Reject.ifNegativeOrZero(maxReadWriteBlockSizeInBytes, "maxReadWriteBlockSizeInBytes");

      this.medium = medium;
      this.name = name;
      this.isRandomAccess = isRandomAccess;
      this.isReadOnly = isReadOnly;
      this.maxCacheSizeInBytes = maxCacheSizeInBytes;
      this.maxReadWriteBlockSizeInBytes = maxReadWriteBlockSizeInBytes;
      this.requiresCaching = requiresCaching;
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
    * @see com.github.jmeta.library.media.api.types.Medium#getMaxCacheSizeInBytes()
    */
   @Override
   public long getMaxCacheSizeInBytes() {
      return maxCacheSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getMaxReadWriteBlockSizeInBytes()
    */
   @Override
   public int getMaxReadWriteBlockSizeInBytes() {
      return maxReadWriteBlockSizeInBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getName()
    */
   @Override
   public String getName() {

      return name;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getWrappedMedium()
    */
   @Override
   public T getWrappedMedium() {

      return medium;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + (medium == null ? 0 : medium.hashCode());
      return result;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#isRandomAccess()
    */
   @Override
   public boolean isRandomAccess() {

      return isRandomAccess;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#isReadOnly()
    */
   @Override
   public boolean isReadOnly() {

      return isReadOnly;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#requiresCaching()
    */
   @Override
   public boolean requiresCaching() {
      return requiresCaching;
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

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "AbstractMedium [medium=" + medium + ", name=" + name + ", isRandomAccess=" + isRandomAccess
         + ", isReadOnly=" + isReadOnly + ", maxCacheSizeInBytes=" + maxCacheSizeInBytes
         + ", maxReadWriteBlockSizeInBytes=" + maxReadWriteBlockSizeInBytes + ", requiresCaching=" + requiresCaching
         + "]";
   }
}
