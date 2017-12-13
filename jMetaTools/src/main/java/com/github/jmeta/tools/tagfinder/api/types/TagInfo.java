/**
 *
 * {@link TagInfo}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package com.github.jmeta.tools.tagfinder.api.types;

/**
 * {@link TagInfo}
 *
 */
public class TagInfo {

   public TagInfo(long absoluteOffset, byte[] tagBytes, int tagSize,
      String[] additionalTagProperties) {
      super();
      m_absoluteOffset = absoluteOffset;
      m_tagBytes = tagBytes;
      m_tagSize = tagSize;
      m_additionalTagProperties = additionalTagProperties;
   }

   /**
    * Returns absoluteOffset
    *
    * @return absoluteOffset
    */
   public long getAbsoluteOffset() {

      return m_absoluteOffset;
   }

   /**
    * Returns tagBytes
    *
    * @return tagBytes
    */
   public byte[] getTagBytes() {

      return m_tagBytes;
   }

   /**
    * Returns tagSize
    *
    * @return tagSize
    */
   public int getDeclaredTagSize() {

      return m_tagSize;
   }

   /**
    * Returns additionalTagProperties
    *
    * @return additionalTagProperties
    */
   public String[] getAdditionalTagProperties() {

      return m_additionalTagProperties;
   }

   private final long m_absoluteOffset;

   private final byte[] m_tagBytes;

   private final int m_tagSize;

   private final String[] m_additionalTagProperties;
}
