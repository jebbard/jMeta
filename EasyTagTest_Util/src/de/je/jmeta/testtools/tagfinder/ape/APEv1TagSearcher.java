/**
 *
 * {@link APEv2TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder.ape;

/**
 * {@link APEv1TagSearcher}
 *
 */
public class APEv1TagSearcher extends AbstractAPETagSearcher {

   /**
    * Creates a new {@APEv2TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public APEv1TagSearcher() {
      super(new byte[] { 'A', 'P', 'E', 'T', 'A', 'G', 'E', 'X', (byte) 0xE8,
         (byte) 0x03, 0, 0 }, new long[] { -32, -160 }, "APEv1");
   }

}
