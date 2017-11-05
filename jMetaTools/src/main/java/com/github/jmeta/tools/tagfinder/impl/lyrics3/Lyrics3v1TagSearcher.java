/**
 *
 * {@link Lyrics3v1TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.lyrics3;

import java.io.RandomAccessFile;

import com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher;

/**
 * {@link Lyrics3v1TagSearcher}
 *
 */
public class Lyrics3v1TagSearcher extends AbstractMagicKeyTagSearcher {

   /**
    * Creates a new {@link Lyrics3v1TagSearcher}.
    */
   public Lyrics3v1TagSearcher() {
      super(new byte[] { 'L', 'Y', 'R', 'I', 'C', 'S', 'E', 'N', 'D' }, new long[] { -137 }, "Lyrics3v1");
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile,
    *      long)
    */
   @Override
   protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) {

      // TODO tagFind001: TagFinder - Implement getting total size for Lyrics 3v1
      return 0;
   }

}
