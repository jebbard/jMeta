/**
 *
 * {@link Lyrics3v2TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder.lyrics3;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher;
import de.je.util.common.charset.Charsets;

/**
 * {@link Lyrics3v2TagSearcher}
 *
 */
public class Lyrics3v2TagSearcher extends AbstractMagicKeyTagSearcher {

   private static final byte[] LYRICS3V2_MAGIC_KEY = new byte[] { 'L', 'Y', 'R',
      'I', 'C', 'S', '2', '0', '0' };

   private static final int LYRICS3V2_TAG_SIZE_LENGTH = 6;

   /**
    * Creates a new {@Lyrics3v2TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public Lyrics3v2TagSearcher() {
      super(LYRICS3V2_MAGIC_KEY, new long[] { -137 }, "Lyrics3v2");
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile, long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset)
      throws IOException {

      ByteBuffer bb = ByteBuffer.allocate(LYRICS3V2_TAG_SIZE_LENGTH);

      file.getChannel().read(bb, possibleOffset - LYRICS3V2_TAG_SIZE_LENGTH);

      byte[] bytes = bb.array();

      final int payloadSize = Integer
         .parseInt(new String(bytes, Charsets.CHARSET_ASCII.name()));

      return payloadSize + LYRICS3V2_TAG_SIZE_LENGTH
         + LYRICS3V2_MAGIC_KEY.length;
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getTagRelativeStartOffset(int)
    */
   @Override
   protected int getTagRelativeStartOffset(int tagSize) {

      return -tagSize + LYRICS3V2_MAGIC_KEY.length;
   }

}
