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

import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;

/**
 * {@link WriteActionSequence} represents a sequence of {@link MediumActionType#WRITE} operations as created by
 * {@link MediumChangeManager#createFlushPlan(int, long)} for insertion bytes and replacement bytes.
 */
public class WriteActionSequence extends ExpectedActionSequence {

   private final ByteBuffer expectedBytes;
   private final int byteBufferStartIndex;

   /**
    * Creates a new {@link WriteActionSequence}.
    * 
    * @param startRef
    *           The start offset for the expected write actions
    * @param blockCount
    *           The number of expected write actions
    * @param blockSizeInBytes
    *           The size of a block to write in bytes
    * @param expectedBytes
    *           The bytes expected to be contained in the write actions
    * @param byteBufferStartIndex
    *           An offset in the byte buffer where the comparison (and dumping) should start
    */
   public WriteActionSequence(MediumOffset startRef, int blockCount, int blockSizeInBytes, ByteBuffer expectedBytes,
      int byteBufferStartIndex) {
      super(startRef, blockCount, blockSizeInBytes);

      this.expectedBytes = expectedBytes;
      this.byteBufferStartIndex = byteBufferStartIndex;
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#assertFollowsSequence(java.util.Iterator)
    */
   @Override
   public void assertFollowsSequence(Iterator<MediumAction> actionIter) {

      MediumOffset nextExpectedWriteRef = getStartRef();

      for (int i = 0; i < getBlockCount(); i++) {
         expectWriteAction(actionIter, nextExpectedWriteRef, getBlockSizeInBytes(), ByteBuffer
            .wrap(expectedBytes.array(), byteBufferStartIndex + i * getBlockSizeInBytes(), getBlockSizeInBytes()));

         nextExpectedWriteRef = nextExpectedWriteRef.advance(getBlockSizeInBytes());
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream stream) {

      MediumOffset nextExpectedWriteRef = getStartRef();

      for (int i = 0; i < getBlockCount(); i++) {
         dumpMediumAction(stream,
            new MediumAction(MediumActionType.WRITE, new MediumRegion(nextExpectedWriteRef, getBlockSizeInBytes()), 0,
               ByteBuffer.wrap(expectedBytes.array(), byteBufferStartIndex + i * getBlockSizeInBytes(),
                  getBlockSizeInBytes())));
         nextExpectedWriteRef = nextExpectedWriteRef.advance(getBlockSizeInBytes());
      }
   }

}
