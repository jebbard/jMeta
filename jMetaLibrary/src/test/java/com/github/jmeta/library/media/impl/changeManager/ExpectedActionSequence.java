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
import java.nio.file.Path;
import java.util.Iterator;

import org.junit.Assert;

import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

import junit.framework.AssertionFailedError;

/**
 * {@link ExpectedActionSequence} represents a sequence (i.e. possibly multiple
 * ordered) {@link MediumAction}s that are expected as a result of a call to the
 * {@link MediumChangeManager#createFlushPlan(int, long)} method. Each of the
 * subclasses represents one type of sequence that can occur, see the
 * sub-classes for details.
 *
 * Basically, each {@link ExpectedActionSequence} has a start
 * {@link MediumOffset} that marks the start offset of processing. Furthermore,
 * it has a number of equally sized blocks. Both the block count and size are
 * given to the constructor of this class.
 */
public abstract class ExpectedActionSequence {

	/**
	 * Checks the given actual {@link MediumAction} against its expected properties,
	 * throws an {@link AssertionFailedError} in case they do not match.
	 * 
	 * @param actual                 The actual {@link MediumAction} to be checked
	 * @param expectedType           The expected {@link MediumActionType} of the
	 *                               {@link MediumAction}
	 * @param expectedStartReference The expected start {@link MediumOffset} of the
	 *                               {@link MediumAction}
	 * @param expectedRegionSize     The expected region size of the
	 *                               {@link MediumAction}
	 * @param expectedBytes          The expected bytes contained in the
	 *                               {@link MediumAction} or null if none are
	 *                               expected. Note that not the position, limit or
	 *                               capacity of the {@link ByteBuffer}s are
	 *                               compared, but only their concrete byte content.
	 */
	protected static void assertMediumActionProperties(MediumAction actual, MediumActionType expectedType,
		MediumOffset expectedStartReference, int expectedRegionSize, ByteBuffer expectedBytes) {
		Assert.assertNotNull(actual);
		Assert.assertEquals(expectedType, actual.getActionType());
		Assert.assertEquals(expectedStartReference, actual.getRegion().getStartOffset());
		Assert.assertEquals(expectedRegionSize, actual.getRegion().getSize());
		if (expectedBytes == null) {
			Assert.assertNull(actual.getActionBytes());
		} else {
			Assert.assertArrayEquals(expectedBytes.array(), actual.getActionBytes().array());
		}
	}

	/**
	 * Dumps a single {@link MediumAction} into the given {@link PrintStream}.
	 * 
	 * @param stream the {@link PrintStream} to dump to
	 * @param action the {@link MediumAction}
	 */
	public static void dumpMediumAction(PrintStream stream, MediumAction action) {
		MediumRegion region = action.getRegion();

		String outputString = action.getActionType() + " at " + region.getStartOffset().getAbsoluteMediumOffset()
			+ " with size " + region.getSize();

		if (action.getActionBytes() != null) {
			outputString += " with bytes " + action.getActionBytes();
		}

		Path mediumPath = (Path) region.getStartOffset().getMedium().getWrappedMedium();
		outputString += " on medium " + mediumPath.getName(mediumPath.getNameCount() - 1);

		stream.println(outputString);
	}

	/**
	 * Asserts a {@link MediumAction} of type {@link MediumActionType#READ} to be
	 * returned next by the given {@link Iterator} of {@link MediumAction}s. Can be
	 * used in the implementation of {@link #assertFollowsSequence(Iterator)} in a
	 * subclass.
	 * 
	 * @param actionIter   The {@link Iterator} to check
	 * @param expectedRef  The expected {@link MediumOffset} of the next
	 *                     {@link MediumAction}.
	 * @param expectedSize The expected size of the next {@link MediumAction}.
	 */
	protected static void expectReadAction(Iterator<MediumAction> actionIter, MediumOffset expectedRef,
		int expectedSize) {
		// Check read block
		Assert.assertTrue(actionIter.hasNext());

		MediumAction nextAction = actionIter.next();

		ExpectedActionSequence.assertMediumActionProperties(nextAction, MediumActionType.READ, expectedRef,
			expectedSize, null);
	}

	/**
	 * Asserts a {@link MediumAction} of type {@link MediumActionType#WRITE} to be
	 * returned next by the given {@link Iterator} of {@link MediumAction}s. Can be
	 * used in the implementation of {@link #assertFollowsSequence(Iterator)} in a
	 * subclass.
	 * 
	 * @param actionIter    The {@link Iterator} to check
	 * @param expectedRef   The expected {@link MediumOffset} of the next
	 *                      {@link MediumAction}.
	 * @param expectedSize  The expected size of the next {@link MediumAction}.
	 * @param expectedBytes The expected bytes contained in the next
	 *                      {@link MediumAction}. Note that not the position, limit
	 *                      or capacity of the {@link ByteBuffer}s are compared, but
	 *                      only their concrete byte content.
	 */
	protected static void expectWriteAction(Iterator<MediumAction> actionIter, MediumOffset expectedRef,
		int expectedSize, ByteBuffer expectedBytes) {
		MediumAction nextAction;
		// Check write block
		Assert.assertTrue(actionIter.hasNext());

		nextAction = actionIter.next();

		ExpectedActionSequence.assertMediumActionProperties(nextAction, MediumActionType.WRITE, expectedRef,
			expectedSize, expectedBytes);
	}

	private final MediumOffset startRef;

	private final int blockCount;

	private final int blockSizeInBytes;

	/**
	 * Creates a new {@link ExpectedActionSequence}.
	 * 
	 * @param startRef         The start reference of the
	 *                         {@link ExpectedActionSequence}.
	 * @param blockCount       The block count, must not be 0 or negative
	 * @param blockSizeInBytes The size of each block in bytes, must not be 0 or
	 *                         negative
	 */
	public ExpectedActionSequence(MediumOffset startRef, int blockCount, int blockSizeInBytes) {
		Reject.ifNull(startRef, "startRef");
		Reject.ifTrue(blockCount < 1, "blockCount < 1");
		Reject.ifTrue(blockSizeInBytes < 1, "blockSizeInBytes < 1");

		this.startRef = startRef;
		this.blockCount = blockCount;
		this.blockSizeInBytes = blockSizeInBytes;
	}

	/**
	 * This method is used by a concrete jUnit test class to check whether the given
	 * {@link Iterator} of {@link MediumAction}s follows the sequence represented by
	 * this {@link ExpectedActionSequence} as expected. If throws an
	 * {@link AssertionFailedError} if this is not the case.
	 * 
	 * @param actionIter The iterator of {@link MediumAction}s, must not be null.
	 */
	public abstract void assertFollowsSequence(Iterator<MediumAction> actionIter);

	/**
	 * Dumps the content of this {@link ExpectedActionSequence} to the given
	 * {@link FilterOutputStream}.
	 * 
	 * 
	 * 
	 * @param stream The {@link FilterOutputStream} to use, usually you might want
	 *               to use {@link System#out} or new PrintStream(new
	 *               FileOutputStream(myFile), true) as argument
	 */
	public abstract void dump(PrintStream stream);

	/**
	 * Returns the block count of this {@link ExpectedActionSequence}.
	 * 
	 * @return the block count of this {@link ExpectedActionSequence}.
	 */
	public int getBlockCount() {
		return blockCount;
	}

	/**
	 * Returns the size of each block in bytes of this
	 * {@link ExpectedActionSequence}.
	 * 
	 * @return the size of each block in bytes of this
	 *         {@link ExpectedActionSequence}.
	 */
	public int getBlockSizeInBytes() {
		return blockSizeInBytes;
	}

	/**
	 * Returns the start {@link MediumOffset} of this
	 * {@link ExpectedActionSequence}.
	 * 
	 * @return the start {@link MediumOffset} of this
	 *         {@link ExpectedActionSequence}.
	 */
	public MediumOffset getStartRef() {
		return startRef;
	}
}
