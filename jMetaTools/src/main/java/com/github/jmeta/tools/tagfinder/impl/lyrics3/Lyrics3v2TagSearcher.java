/**
 *
 * {@link Lyrics3v2TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.lyrics3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link Lyrics3v2TagSearcher}
 *
 */
public class Lyrics3v2TagSearcher extends AbstractMagicKeyTagSearcher {

   private static final byte[] LYRICS3V2_MAGIC_KEY = new byte[] { 'L', 'Y', 'R', 'I', 'C', 'S', '2', '0', '0' };

   private static final int LYRICS3V2_TAG_SIZE_LENGTH = 6;

   /**
    * Creates a new {@link Lyrics3v2TagSearcher}.
    */
   public Lyrics3v2TagSearcher() {
      super(LYRICS3V2_MAGIC_KEY, new long[] { -137 }, "Lyrics3v2");
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile,
    *      long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) throws IOException {

      ByteBuffer bb = ByteBuffer.allocate(LYRICS3V2_TAG_SIZE_LENGTH);

      file.getChannel().read(bb, possibleOffset - LYRICS3V2_TAG_SIZE_LENGTH);

      byte[] bytes = bb.array();

      final int payloadSize = Integer.parseInt(new String(bytes, Charsets.CHARSET_ASCII.name()));

      return payloadSize + LYRICS3V2_TAG_SIZE_LENGTH + LYRICS3V2_MAGIC_KEY.length;
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTagRelativeStartOffset(int)
    */
   @Override
   protected int getTagRelativeStartOffset(int tagSize) {

      return -tagSize + LYRICS3V2_MAGIC_KEY.length;
   }

}
