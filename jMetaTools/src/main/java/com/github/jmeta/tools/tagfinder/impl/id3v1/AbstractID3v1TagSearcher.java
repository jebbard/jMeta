
package com.github.jmeta.tools.tagfinder.impl.id3v1;

import java.io.RandomAccessFile;

import com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher;
import com.github.jmeta.tools.tagfinder.api.types.TagInfo;

public abstract class AbstractID3v1TagSearcher extends AbstractMagicKeyTagSearcher {

   private static final int COMMENT_NULL_BYTE_OFFSET = 125;

   public static final int ID3V1_TAG_SIZE = 128;

   public AbstractID3v1TagSearcher(String tagName) {
      super(new byte[] { 'T', 'A', 'G' }, new long[] { -ID3V1_TAG_SIZE }, tagName);
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTagInfo(java.io.RandomAccessFile)
    */
   @Override
   public TagInfo getTagInfo(RandomAccessFile file) {

      final TagInfo tagInfo = super.getTagInfo(file);
      if (tagInfo != null) {
         byte[] idBytes = new byte[2];

         idBytes[0] = tagInfo.getTagBytes()[COMMENT_NULL_BYTE_OFFSET];
         idBytes[1] = tagInfo.getTagBytes()[COMMENT_NULL_BYTE_OFFSET + 1];

         if (!isTagPresent(idBytes))
            return null;
      }

      return tagInfo;
   }

   protected abstract boolean isTagPresent(byte[] idBytes);

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile,
    *      long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) {

      return ID3V1_TAG_SIZE;
   }

}