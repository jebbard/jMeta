/**
 *
 * {@link ID3v1TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder.id3v1;

/**
 * {@link ID3v11TagSearcher}
 *
 */
public class ID3v11TagSearcher extends AbstractID3v1TagSearcher {

   /**
    * Creates a new {@ID3v1TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public ID3v11TagSearcher() {
      super("ID3v1.1");
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.id3v1.AbstractID3v1TagSearcher#isTagPresent(byte[])
    */
   @Override
   protected boolean isTagPresent(byte[] bytes) {

      return bytes[0] == 0 && bytes[1] != 0;
   }
}
