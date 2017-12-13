/**
 *
 * {@link APEv2TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.ape;

/**
 * {@link APEv2TagSearcher}
 *
 */
public class APEv2TagSearcher extends AbstractAPETagSearcher {

   /**
    * Creates a new {@link APEv2TagSearcher}.
    */
   public APEv2TagSearcher() {
      super(new byte[] { 'A', 'P', 'E', 'T', 'A', 'G', 'E', 'X', (byte) 0xD0, (byte) 0x07, 0, 0 },
         new long[] { 0, -32, -160 }, "APEv2");
   }
}
