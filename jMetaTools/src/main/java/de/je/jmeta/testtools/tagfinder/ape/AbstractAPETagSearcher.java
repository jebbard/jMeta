
package de.je.jmeta.testtools.tagfinder.ape;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher;

public abstract class AbstractAPETagSearcher
   extends AbstractMagicKeyTagSearcher {

   private static final int APE_TAG_HEADER_SIZE = 32;

   public AbstractAPETagSearcher(byte[] magicKey, long[] possibleOffsets,
      String tagName) {
      super(magicKey, possibleOffsets, tagName);
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getAdditionalInfo(java.nio.ByteBuffer)
    */
   @Override
   protected String[] getAdditionalInfo(ByteBuffer tagBytes)
      throws IOException {

      APETagHeaderInfo info = getTagHeaderInfo(tagBytes);

      String itemCount = "Item Count: " + info.getItemCount();
      String isReadOnly = "Is Read Only: " + ((info.getFlags()[0] & 0x01) != 0);
      String isTheHeader = "This is the header: "
         + ((info.getFlags()[3] & 32) != 0);
      String hasFooter = "Has footer: " + ((info.getFlags()[3] & 64) != 1);
      String hasHeader = "Has header: " + ((info.getFlags()[3] & 128) != 0);

      return new String[] { itemCount, isReadOnly, isTheHeader, hasHeader,
         hasFooter };
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile, long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset)
      throws IOException {

      ByteBuffer bb = ByteBuffer.allocate(APE_TAG_HEADER_SIZE);

      file.getChannel().read(bb, possibleOffset);

      APETagHeaderInfo info = getTagHeaderInfo(bb);

      return info.getTagSize();
   }

   protected int getTagRelativeStartOffset(int tagSize) {

      // Tag size given in footer, therefore tag bytes are located before footer
      // ATTENTION: This only works correct for APE tags at the end of file
      return -tagSize + APE_TAG_HEADER_SIZE;
   }

   /**
    * @param bb
    * @return
    */
   private APETagHeaderInfo getTagHeaderInfo(ByteBuffer bb) {

      bb.rewind();
      bb.order(ByteOrder.LITTLE_ENDIAN);

      long preample = bb.getLong();
      int version = bb.getInt();
      int tagSize = bb.getInt();
      int itemCount = bb.getInt();
      byte[] flags = new byte[4];
      bb.get(flags);
      boolean hasFooter = (flags[3] & 64) != 1;
      boolean hasHeader = (flags[3] & 128) != 0;

      // If both header and footer are present, then one of them is excluded in the
      // size encoded in the tag and therefore must be added to get the total tag size
      if (hasHeader && hasFooter)
         tagSize += APE_TAG_HEADER_SIZE;

      return new APETagHeaderInfo(preample, version, tagSize, itemCount, flags);
   }

}