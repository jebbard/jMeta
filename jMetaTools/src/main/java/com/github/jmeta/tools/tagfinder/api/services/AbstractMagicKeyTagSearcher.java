/**
 *
 * {@link AbstractMagicKeyTagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.api.services;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.jmeta.tools.tagfinder.api.types.TagInfo;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractMagicKeyTagSearcher}
 *
 */
public abstract class AbstractMagicKeyTagSearcher implements ITagSearcher {

   public AbstractMagicKeyTagSearcher(byte[] magicKey, long[] possibleOffsets,
      String tagName) {
      Reject.ifNull(magicKey, "magicKey");
      Reject.ifNull(possibleOffsets, "possibleOffsets");

      m_magicKey = magicKey;
      m_possibleOffsets = possibleOffsets;
      setTagName(tagName);
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.ITagSearcher#getTagName()
    */
   public String getTagName() {

      return m_tagName;
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.ITagSearcher#getTagInfo(RandomAccessFile)
    */
   public TagInfo getTagInfo(RandomAccessFile file) {

      Reject.ifNull(file, "file");

      int byteCount = m_magicKey.length;

      for (int i = 0; i < m_possibleOffsets.length; i++) {
         long possibleOffset = m_possibleOffsets[i];

         long fileSize;

         try {
            fileSize = file.length();

            if (possibleOffset < 0) {
               possibleOffset = fileSize + possibleOffset;
            }

            // The offset is not within file range: No tag can be there
            if (possibleOffset < 0 || fileSize < possibleOffset + byteCount)
               continue;

            byte[] readBytes = readTheDamnBytes(file, possibleOffset,
               byteCount);

            if (Arrays.equals(m_magicKey, readBytes)) {
               int tagSize = getTotalTagSize(file, possibleOffset);
               byte[] tagBytes = readTheDamnBytes(file,
                  possibleOffset + getTagRelativeStartOffset(tagSize), tagSize);

               ByteBuffer bb = ByteBuffer.wrap(tagBytes);

               String[] additionalTagInfo = getAdditionalInfo(bb);

               return new TagInfo(possibleOffset, tagBytes, tagSize,
                  additionalTagInfo);
            }
         }

         catch (IOException e) {
            throw new IllegalStateException("IO exception: " + e);
         }
      }

      return null;
   }

   protected String[] getAdditionalInfo(ByteBuffer tagBytes)
      throws IOException {

      return new String[] {};
   }

   protected int getTagRelativeStartOffset(int tagSize) {

      return 0;
   }

   protected abstract int getTotalTagSize(RandomAccessFile file,
      long possibleOffset) throws IOException;

   private byte[] readTheDamnBytes(RandomAccessFile file, long offset,
      int byteCount) throws IOException {

      final byte[] returnedBytes = new byte[byteCount];
      ByteBuffer readBytes = ByteBuffer.wrap(returnedBytes);

      int bytesRead = file.getChannel().read(readBytes, offset);

      if (bytesRead == -1)
         throw new IllegalStateException(
            "Unexpected EOF in file " + file + " at offst " + offset);

      return returnedBytes;
   }

   protected void setTagName(String name) {

      Reject.ifNull(name, "name");

      m_tagName = name;
   }

   private final byte[] m_magicKey;

   private String m_tagName;

   private final long[] m_possibleOffsets;
}
