/**
 *
 * {@link ID3v1TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder.id3v1;

import java.io.RandomAccessFile;

import de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher;

/**
 * {@link ID3v1EnhancedTagSearcher}
 *
 */
public class ID3v1EnhancedTagSearcher extends AbstractMagicKeyTagSearcher {

   private static final int ID3V1_ENHANCED_TAG_SIZE = 227;

   /**
    * Creates a new {@ID3v1TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public ID3v1EnhancedTagSearcher() {
      super(new byte[] { 'T', 'A', 'G', '+' }, new long[] {
         -AbstractID3v1TagSearcher.ID3V1_TAG_SIZE - ID3V1_ENHANCED_TAG_SIZE },
         "Enhanced ID3v1");
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile, long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) {

      return ID3V1_ENHANCED_TAG_SIZE;
   }
}
