/**
 *
 * WriteActionSequence.java
 *
 * @author Jens
 *
 * @date 11.10.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;

/**
 * {@link WriteActionSequence}
 *
 */
public class WriteActionSequence extends ExpectedActionSequence {

   private final ByteBuffer expectedBytes;

   /**
    * Creates a new {@link WriteActionSequence}.
    * 
    * @param startRef
    * @param blockCount
    * @param blockSizeInBytes
    * @param expectedBytes
    */
   public WriteActionSequence(MediumReference startRef, int blockCount, int blockSizeInBytes,
      ByteBuffer expectedBytes) {
      super(startRef, blockCount, blockSizeInBytes);

      this.expectedBytes = expectedBytes;
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#assertFollowsSequence(java.util.Iterator)
    */
   @Override
   public void assertFollowsSequence(Iterator<MediumAction> actionIter) {

      MediumReference nextExpectedWriteRef = getStartRef();

      for (int i = 0; i < getBlockCount(); i++) {
         expectWriteAction(actionIter, nextExpectedWriteRef, getBlockSizeInBytes(),
            ByteBuffer.wrap(expectedBytes.array(), i * getBlockSizeInBytes(), getBlockSizeInBytes()));

         nextExpectedWriteRef = nextExpectedWriteRef.advance(getBlockSizeInBytes());
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream stream) {

      MediumReference nextExpectedWriteRef = getStartRef();

      for (int i = 0; i < getBlockCount(); i++) {
         dumpMediumAction(stream,
            new MediumAction(MediumActionType.WRITE, new MediumRegion(nextExpectedWriteRef, getBlockSizeInBytes()), 0,
               ByteBuffer.wrap(expectedBytes.array(), i * getBlockSizeInBytes(), getBlockSizeInBytes())));
         nextExpectedWriteRef = nextExpectedWriteRef.advance(getBlockSizeInBytes());
      }
   }

}
