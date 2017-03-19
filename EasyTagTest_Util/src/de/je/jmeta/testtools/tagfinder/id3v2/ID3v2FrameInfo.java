/**
 *
 * {@link ID3v2FrameProperties}.java
 *
 * @author Jens Ebert
 *
 * @date 10.03.2011
 */
package de.je.jmeta.testtools.tagfinder.id3v2;

import java.io.UnsupportedEncodingException;

import de.je.util.common.charset.Charsets;
import de.je.util.common.err.Reject;

/**
 * {@link ID3v2FrameProperties}
 *
 */
public class ID3v2FrameInfo {

   public ID3v2FrameInfo(byte[] idBytes, int size, byte[] flags) {
      Reject.ifNull(idBytes, "idBytes");
      Reject.ifNull(flags, "flags");

      try {
         m_id = new String(idBytes, Charsets.CHARSET_ASCII.name());
      } catch (UnsupportedEncodingException e) {
         throw new RuntimeException("Unsupported encoding", e);
      }
      m_size = size;
      m_flags = flags;
   }

   /**
    * Returns id
    *
    * @return id
    */
   public String getId() {

      return m_id;
   }

   /**
    * Returns size
    *
    * @return size
    */
   public int getSize() {

      return m_size;
   }

   /**
    * Returns flags
    *
    * @return flags
    */
   public byte[] getFlags() {

      return m_flags;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getId() + " - actual size: " + getSize();
   }

   private final String m_id;

   private final int m_size;

   private final byte[] m_flags;
}
