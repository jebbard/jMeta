/**
 *
 * ExpectedActionSequence.java
 *
 * @author Jens
 *
 * @date 11.10.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.io.FilterOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

import junit.framework.AssertionFailedError;

import org.junit.Assert;

import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.MediumAction;
import com.github.jmeta.library.media.api.type.MediumActionType;
import com.github.jmeta.library.media.api.type.MediumRegion;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link ExpectedActionSequence} represents a sequence (i.e. possibly multiple ordered) {@link MediumAction}s that are
 * expected as a result of a call to the {@link MediumChangeManager#createFlushPlan(int, long)} method. Each of the
 * subclasses represents one type of sequence that can occur, see the sub-classes for details.
 * 
 * Basically, each {@link ExpectedActionSequence} has a start {@link IMediumReference} that marks the start offset of
 * processing. Furthermore, it has a number of equally sized blocks. Both the block count and size are given to the
 * constructor of this class.
 */
public abstract class ExpectedActionSequence {

   private final IMediumReference startRef;
   private final int blockCount;
   private final int blockSizeInBytes;

   /**
    * Creates a new {@link ExpectedActionSequence}.
    * 
    * @param startRef
    *           The start reference of the {@link ExpectedActionSequence}.
    * @param blockCount
    *           The block count, must not be 0 or negative
    * @param blockSizeInBytes
    *           The size of each block in bytes, must not be 0 or negative
    */
   public ExpectedActionSequence(IMediumReference startRef, int blockCount, int blockSizeInBytes) {
      Reject.ifNull(startRef, "startRef");
      Reject.ifTrue(blockCount < 1, "blockCount < 1");
      Reject.ifTrue(blockSizeInBytes < 1, "blockSizeInBytes < 1");

      this.startRef = startRef;
      this.blockCount = blockCount;
      this.blockSizeInBytes = blockSizeInBytes;
   }

   /**
    * Returns the start {@link IMediumReference} of this {@link ExpectedActionSequence}.
    * 
    * @return the start {@link IMediumReference} of this {@link ExpectedActionSequence}.
    */
   public IMediumReference getStartRef() {
      return startRef;
   }

   /**
    * Returns the block count of this {@link ExpectedActionSequence}.
    * 
    * @return the block count of this {@link ExpectedActionSequence}.
    */
   public int getBlockCount() {
      return blockCount;
   }

   /**
    * Returns the size of each block in bytes of this {@link ExpectedActionSequence}.
    * 
    * @return the size of each block in bytes of this {@link ExpectedActionSequence}.
    */
   public int getBlockSizeInBytes() {
      return blockSizeInBytes;
   }

   /**
    * This method is used by a concrete jUnit test class to check whether the given {@link Iterator} of
    * {@link MediumAction}s follows the sequence represented by this {@link ExpectedActionSequence} as expected. If
    * throws an {@link AssertionFailedError} if this is not the case.
    * 
    * @param actionIter
    *           The iterator of {@link MediumAction}s, must not be null.
    */
   public abstract void assertFollowsSequence(Iterator<MediumAction> actionIter);

   /**
    * Dumps the content of this {@link ExpectedActionSequence} to the given {@link FilterOutputStream}.
    * 
    * 
    * 
    * @param stream
    *           The {@link FilterOutputStream} to use, usually you might want to use {@link System#out} or new
    *           PrintStream(new FileOutputStream(myFile), true) as argument
    */
   public abstract void dump(PrintStream stream);

   /**
    * Dumps a single {@link MediumAction} into the given {@link PrintStream}.
    * 
    * @param stream
    *           the {@link PrintStream} to dump to
    * @param action
    *           the {@link MediumAction}
    */
   public static void dumpMediumAction(PrintStream stream, MediumAction action) {
      MediumRegion region = action.getRegion();

      String outputString = action.getActionType() + " at " + region.getStartReference().getAbsoluteMediumOffset()
         + " with size " + region.getSize();

      if (action.getActionBytes() != null) {
         outputString += " with bytes " + action.getActionBytes();
      }

      outputString += " on medium " + region.getStartReference().getMedium().getName();

      stream.println(outputString);
   }

   /**
    * Asserts a {@link MediumAction} of type {@link MediumActionType#WRITE} to be returned next by the given
    * {@link Iterator} of {@link MediumAction}s. Can be used in the implementation of
    * {@link #assertFollowsSequence(Iterator)} in a subclass.
    * 
    * @param actionIter
    *           The {@link Iterator} to check
    * @param expectedRef
    *           The expected {@link IMediumReference} of the next {@link MediumAction}.
    * @param expectedSize
    *           The expected size of the next {@link MediumAction}.
    * @param expectedBytes
    *           The expected bytes contained in the next {@link MediumAction}. Note that not the position, limit or
    *           capacity of the {@link ByteBuffer}s are compared, but only their concrete byte content.
    */
   protected static void expectWriteAction(Iterator<MediumAction> actionIter, IMediumReference expectedRef,
      int expectedSize, ByteBuffer expectedBytes) {
      MediumAction nextAction;
      // Check write block
      Assert.assertTrue(actionIter.hasNext());

      nextAction = actionIter.next();

      assertMediumActionProperties(nextAction, MediumActionType.WRITE, expectedRef, expectedSize, expectedBytes);
   }

   /**
    * Asserts a {@link MediumAction} of type {@link MediumActionType#READ} to be returned next by the given
    * {@link Iterator} of {@link MediumAction}s. Can be used in the implementation of
    * {@link #assertFollowsSequence(Iterator)} in a subclass.
    * 
    * @param actionIter
    *           The {@link Iterator} to check
    * @param expectedRef
    *           The expected {@link IMediumReference} of the next {@link MediumAction}.
    * @param expectedSize
    *           The expected size of the next {@link MediumAction}.
    */
   protected static void expectReadAction(Iterator<MediumAction> actionIter, IMediumReference expectedRef,
      int expectedSize) {
      // Check read block
      Assert.assertTrue(actionIter.hasNext());

      MediumAction nextAction = actionIter.next();

      assertMediumActionProperties(nextAction, MediumActionType.READ, expectedRef, expectedSize, null);
   }

   /**
    * Checks the given actual {@link MediumAction} against its expected properties, throws an
    * {@link AssertionFailedError} in case they do not match.
    * 
    * @param actual
    *           The actual {@link MediumAction} to be checked
    * @param expectedType
    *           The expected {@link MediumActionType} of the {@link MediumAction}
    * @param expectedStartReference
    *           The expected start {@link IMediumReference} of the {@link MediumAction}
    * @param expectedRegionSize
    *           The expected region size of the {@link MediumAction}
    * @param expectedBytes
    *           The expected bytes contained in the {@link MediumAction} or null if none are expected. Note that not the
    *           position, limit or capacity of the {@link ByteBuffer}s are compared, but only their concrete byte
    *           content.
    */
   protected static void assertMediumActionProperties(MediumAction actual, MediumActionType expectedType,
      IMediumReference expectedStartReference, int expectedRegionSize, ByteBuffer expectedBytes) {
      Assert.assertNotNull(actual);
      Assert.assertEquals(expectedType, actual.getActionType());
      Assert.assertEquals(expectedStartReference, actual.getRegion().getStartReference());
      Assert.assertEquals(expectedRegionSize, actual.getRegion().getSize());
      if (expectedBytes == null) {
         Assert.assertNull(actual.getActionBytes());
      } else {
         Assert.assertArrayEquals(expectedBytes.array(), actual.getActionBytes().array());
      }
   }
}
