/**
 *
 * ReadWriteActionSequence.java
 *
 * @author Jens
 *
 * @date 11.10.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.function.BiConsumer;

import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ReadWriteActionSequence} represents a sequence of alternating {@link MediumActionType#READ} and
 * {@link MediumActionType#WRITE} {@link MediumAction}s. In such a sequence, always a READ comes first, then a WRITE.
 * 
 * The number of such READ-WRITE-pairs is determined by {@link #getBlockCount()}, each having the same size of
 * {@link #getBlockSizeInBytes()}.
 * 
 * Every {@link ReadWriteActionSequence} has an order of processing, either forward or backward, specified by the test
 * case and determined by the field {@link #expectedActionOrder}.
 * 
 * Saying this, the given start reference of the {@link ReadWriteActionSequence} returned by {@link #getStartRef()} is
 * either pointing to the front of the byte area this {@link ReadWriteActionSequence} encompasses (in case of
 * forward-reading), or pointing to its end (in case of forward-reading).
 */
public class ReadWriteActionSequence extends ExpectedActionSequence {

   /**
    * {@link ActionOrder} is just an enum indicating the expected read-write order of the
    * {@link ReadWriteActionSequence}. It is meant for better test case readability.
    */
   public static enum ActionOrder {
      /**
       * Indicates forward read-write order (i.e. returing {@link MediumAction}s with increasing offset).
       */
      FORWARD,
      /**
       * Indicates backward read-write order (i.e. returing {@link MediumAction}s with decreasing offset).
       */
      BACKWARD
   }

   private final int relativeWriteShiftInBytes;
   private final ActionOrder expectedActionOrder;

   /**
    * Creates a new {@link ReadWriteActionSequence}.
    * 
    * @param startRef
    *           The {@link IMediumReference} that marks where to start processing this {@link ReadWriteActionSequence}.
    *           Either pointing to the front of the byte area this {@link ReadWriteActionSequence} encompasses (in case
    *           of forward-reading), or pointing to its end (in case of forward-reading).
    * @param blockCount
    *           The number of equal-sized read-write blocks contained in this {@link ReadWriteActionSequence}.
    * @param blockSizeInBytes
    *           The size of each read and each write block.
    * @param relativeWriteShiftInBytes
    *           Allows to specify an arbitrary byte shift that is added to every write (but NOT read!) action.
    * @param expectedActionOrder
    *           Indicates whether this {@link ReadWriteActionSequence} is generating {@link MediumAction} by
    *           forward-shifts, i.e. by increasing the offset after each block pair, or by decrementing the offset after
    *           each block pair.
    */
   public ReadWriteActionSequence(IMediumReference startRef, int blockCount, int blockSizeInBytes,
      int relativeWriteShiftInBytes, ActionOrder expectedActionOrder) {
      super(startRef, blockCount, blockSizeInBytes);
      Reject.ifNull(expectedActionOrder, "expectedActionOrder");

      this.expectedActionOrder = expectedActionOrder;
      this.relativeWriteShiftInBytes = relativeWriteShiftInBytes;
   }

   /**
    * Convenience factory for creating a {@link ReadWriteActionSequence} with a single block. In this case, it does not
    * matter whether forward or backward processing is done, thus the action order is always set to
    * {@link ActionOrder#FORWARD}.
    * 
    * @param startRef
    *           The {@link IMediumReference} that marks where to start processing this {@link ReadWriteActionSequence}.
    *           Either pointing to the front of the byte area this {@link ReadWriteActionSequence} encompasses (in case
    *           of forward-reading), or pointing to its end (in case of forward-reading).
    * @param blockSizeInBytes
    *           The size of each read and each write block.
    * @param relativeWriteShiftInBytes
    *           Allows to specify an arbitrary byte shift that is added to every write (but NOT read!) action.
    * @return A single-blocked {@link ReadWriteActionSequence}.
    */
   public static ReadWriteActionSequence createSingleBlock(IMediumReference startRef, int blockSizeInBytes,
      int relativeWriteShiftInBytes) {
      return new ReadWriteActionSequence(startRef, 1, blockSizeInBytes, relativeWriteShiftInBytes, ActionOrder.FORWARD);
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#assertFollowsSequence(java.util.Iterator)
    */
   @Override
   public void assertFollowsSequence(Iterator<MediumAction> actionIter) {
      iterateBlock((ref, size) -> expectReadAction(actionIter, ref, size),
         (ref, size) -> expectWriteAction(actionIter, ref, size, null));
   }

   /**
    * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#dump(PrintStream)
    */
   @Override
   public void dump(PrintStream stream) {
      iterateBlock(
         (ref, size) -> dumpMediumAction(stream,
            new MediumAction(MediumActionType.READ, new MediumRegion(ref, size), 0, null)),
         (ref, size) -> dumpMediumAction(stream,
            new MediumAction(MediumActionType.WRITE, new MediumRegion(ref, size), 0, null)));
   }

   private void iterateBlock(BiConsumer<IMediumReference, Integer> readAction,
      BiConsumer<IMediumReference, Integer> writeAction) {

      IMediumReference nextExpectedReadRef = getStartRef();
      IMediumReference nextExpectedWriteRef = nextExpectedReadRef.advance(relativeWriteShiftInBytes);

      for (int i = 0; i < getBlockCount(); i++) {
         if (expectedActionOrder == ActionOrder.BACKWARD) {
            nextExpectedReadRef = nextExpectedReadRef.advance(-getBlockSizeInBytes());
            nextExpectedWriteRef = nextExpectedWriteRef.advance(-getBlockSizeInBytes());
         }

         readAction.accept(nextExpectedReadRef, getBlockSizeInBytes());
         writeAction.accept(nextExpectedWriteRef, getBlockSizeInBytes());

         if (expectedActionOrder == ActionOrder.FORWARD) {
            nextExpectedReadRef = nextExpectedReadRef.advance(getBlockSizeInBytes());
            nextExpectedWriteRef = nextExpectedWriteRef.advance(getBlockSizeInBytes());
         }
      }
   }

}
