/**
 *
 * {@link ID3v2TagHeaderInfo}.java
 *
 * @author jebert
 *
 * @date 03.03.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v2;

/**
 * {@link ID3v2TagHeaderInfo}
 *
 */
public class ID3v2TagHeaderInfo {

   private final short m_version;

   private final int m_tagSize;

   private final byte m_flags;

   private final byte[] m_id;

   public ID3v2TagHeaderInfo(byte[] id, short version, byte flags, int tagSize) {
      super();
      m_id = id;
      m_version = version;
      m_flags = flags;
      m_tagSize = tagSize;
   }

   /**
    * Returns version
    *
    * @return version
    */
   public short getVersion() {

      return m_version;
   }

   /**
    * Returns tagSize
    *
    * @return tagSize
    */
   public int getTagSize() {

      return m_tagSize;
   }

   /**
    * Returns flags
    *
    * @return flags
    */
   public byte getFlags() {

      return m_flags;
   }

   /**
    * Returns id
    *
    * @return id
    */
   public byte[] getId() {

      return m_id;
   }
}
