/**
 * {@link LocationProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

/**
 *
 */
public class LocationProperties {

   /**
    * Creates a new {@link LocationProperties}.
    * 
    * @param byteOffset
    * @param minOccurrences
    * @param maxOccurrences
    */
   public LocationProperties(long byteOffset, int minOccurrences, int maxOccurrences) {

      m_byteOffset = byteOffset;
      m_maxOccurrences = maxOccurrences;
      m_minOccurrences = minOccurrences;
   }

   /**
    * @return byte offset
    */
   public long getByteOffset() {

      return m_byteOffset;
   }

   /**
    * @return max occurrences
    */
   public int getMaxOccurrences() {

      return m_maxOccurrences;
   }

   /**
    * @return min occurrences
    */
   public int getMinOccurrences() {

      return m_minOccurrences;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + ", byteOffset=" + m_byteOffset + ", maxOccurrences=" + m_maxOccurrences
         + ", minOccurrences=" + m_minOccurrences + "]";
   }

   private final long m_byteOffset;

   private final int m_maxOccurrences;

   private final int m_minOccurrences;

}
