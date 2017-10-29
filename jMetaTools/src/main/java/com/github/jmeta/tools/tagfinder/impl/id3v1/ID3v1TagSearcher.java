/**
 *
 * {@link ID3v1TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v1;

/**
 * {@link ID3v1TagSearcher}
 *
 */
public class ID3v1TagSearcher extends AbstractID3v1TagSearcher {

   /**
    * Creates a new {@ID3v1TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public ID3v1TagSearcher() {
      super("ID3v1");
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.impl.id3v1.AbstractID3v1TagSearcher#isTagPresent(byte[])
    */
   @Override
   protected boolean isTagPresent(byte[] bytes) {

      return bytes[0] != 0 || bytes[1] == 0;
   }
}
