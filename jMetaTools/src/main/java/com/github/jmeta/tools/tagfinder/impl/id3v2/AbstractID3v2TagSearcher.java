
package com.github.jmeta.tools.tagfinder.impl.id3v2;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher;

public abstract class AbstractID3v2TagSearcher extends AbstractMagicKeyTagSearcher {

   private static final int FROM_CONVERSION_MASK = 0x7F000000;
   protected static final int ID3V2_TAG_HEADER_SIZE = 10;

   public AbstractID3v2TagSearcher(byte[] magicKey, long[] possibleOffsets, String tagName) {
      super(magicKey, possibleOffsets, tagName);
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile,
    *      long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) throws IOException {

      ByteBuffer bb = ByteBuffer.allocate(ID3V2_TAG_HEADER_SIZE);

      file.getChannel().read(bb, possibleOffset);

      ID3v2TagHeaderInfo info = getTagHeaderInfo(bb);

      return info.getTagSize();
   }

   protected ID3v2TagHeaderInfo getTagHeaderInfo(ByteBuffer bb) {

      bb.rewind();
      bb.order(ByteOrder.BIG_ENDIAN);

      byte[] id = new byte[3];
      bb.get(id);
      short version = bb.getShort();
      byte flags = bb.get();
      int totalTagSize = synchSafeToInt(bb.getInt()) + ID3V2_TAG_HEADER_SIZE;

      return new ID3v2TagHeaderInfo(id, version, flags, totalTagSize);
   }

   /**
    * Converts a synchsafe integer to a usual integer.
    *
    * @param synchSafeInteger
    *           The synchsafe integer to convert.
    * @return The usual integer.
    */
   private static int synchSafeToInt(int synchSafeInteger) {

      int usualInteger = 0;
      int mask = FROM_CONVERSION_MASK;

      for (int i = 0; i < Integer.SIZE / Byte.SIZE; ++i) {
         usualInteger >>= 1;
         usualInteger |= synchSafeInteger & mask;
         mask >>= Byte.SIZE;
      }

      return usualInteger;
   }
}