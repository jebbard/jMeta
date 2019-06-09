/**
 *
 * MediumActionComparatorTest.java
 *
 * @author Jens
 *
 * @date 23.05.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link MediumActionComparatorTest} tests the class
 * {@link MediumActionComparator}.
 */
public class MediumActionComparatorTest {

	private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forEqualMediumActions_returnsZero() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumAction leftInsert = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightInsert = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertEquals(0, comparator.compare(leftInsert, rightInsert));

		MediumAction leftWrite = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(22), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightWrite = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(22), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertEquals(0, comparator.compare(leftWrite, rightWrite));

		MediumAction leftReplace = new MediumAction(MediumActionType.REPLACE,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightReplace = new MediumAction(MediumActionType.REPLACE,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertEquals(0, comparator.compare(leftReplace, rightReplace));
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forLeftAndRightInsertLeftWithSmallerSequenceNumber_atSameOffset_returnsNegativeInt() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumOffset offset = TestMedia.at(0);

		// Sequence number of left object is smaller than right one
		MediumAction leftAction = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightAction = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue(comparator.compare(leftAction, rightAction) < 0);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forLeftInsertActionAndRightRemoveAction_atSameOffset_returnsNegativeInt() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumOffset offset = TestMedia.at(0);

		// Sequence number of left object is smaller than right one
		MediumAction leftAction = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightAction = new MediumAction(MediumActionType.REMOVE,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2, null);

		Assert.assertTrue(comparator.compare(leftAction, rightAction) < 0);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forLeftInsertActionAndRightReplaceAction_atSameOffset_returnsNegativeInt() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumOffset offset = TestMedia.at(0);

		// Sequence number of left object is smaller than right one
		MediumAction leftAction = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction rightAction = new MediumAction(MediumActionType.REPLACE,
			new MediumRegion(offset, MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue(comparator.compare(leftAction, rightAction) < 0);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test(expected = NullPointerException.class)
	public void compare_forLeftMediumActionNull_throwsNullPointerException() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);

		comparator.compare(null, actionA1);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forLeftMediumActionsBigger_returnsPositiveInt() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumAction actionALeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionARight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue("Sequence number of left object is bigger than right one",
			comparator.compare(actionALeft, actionARight) > 0);

		MediumAction actionBLeft = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(9999), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionBRight = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(22), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue("MediumReference offset of left object is bigger than right one, sequence number identical",
			comparator.compare(actionBLeft, actionBRight) > 0);

		MediumAction actionCLeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(100), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionCRight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue(
			"MediumReference offset of left object is bigger than right one, and sequence number is smaller than right one",
			comparator.compare(actionCLeft, actionCRight) > 0);

		MediumAction actionDLeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(100), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionDRight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue("Both MediumReference offset and sequence number of left object is bigger than right one",
			comparator.compare(actionDLeft, actionDRight) > 0);

		MediumAction actionELeft = new MediumAction(MediumActionType.REPLACE, new MediumRegion(TestMedia.at(100), 11),
			0, MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionERight = new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(100), 1), 0,
			ByteBuffer.wrap(new byte[] { 1 }));

		Assert.assertTrue(
			"Both MediumReference offset and sequence number are equal, but Action type and region size differ",
			comparator.compare(actionELeft, actionERight) > 0);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void compare_forLeftMediumActionsSmaller_returnsNegativeInt() {

		MediumActionComparator comparator = new MediumActionComparator();

		// Sequence number of left object is smaller than right one
		MediumAction actionALeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionARight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 2,
			MediumActionComparatorTest.DEFAULT_BYTES);
		// MediumReference offset of left object is smaller than right one, sequence
		// number identical
		MediumAction actionBLeft = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(22), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionBRight = new MediumAction(MediumActionType.WRITE,
			new MediumRegion(TestMedia.at(9999), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 122,
			MediumActionComparatorTest.DEFAULT_BYTES);
		// MediumReference offset of left object is smaller than right one, and sequence
		// number is bigger than right one
		MediumAction actionCLeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionCRight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(100), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);
		// Both MediumReference offset and sequence number of left object is smaller
		// than right one
		MediumAction actionDLeft = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionDRight = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(100), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 1,
			MediumActionComparatorTest.DEFAULT_BYTES);

		Assert.assertTrue(comparator.compare(actionALeft, actionARight) < 0);
		Assert.assertTrue(comparator.compare(actionBLeft, actionBRight) < 0);
		Assert.assertTrue(comparator.compare(actionCLeft, actionCRight) < 0);
		Assert.assertTrue(comparator.compare(actionDLeft, actionDRight) < 0);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void compare_forMediumActionsWithDifferentMedium_throwsException() {

		MediumActionComparator comparator = new MediumActionComparator();

		FileMedium otherMedium = new FileMedium(Paths.get("."), true);

		MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);
		MediumAction actionA2 = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(new StandardMediumOffset(otherMedium, 0),
				MediumActionComparatorTest.DEFAULT_BYTES.remaining()),
			0, MediumActionComparatorTest.DEFAULT_BYTES);

		comparator.compare(actionA1, actionA2);
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test(expected = NullPointerException.class)
	public void compare_forRightMediumActionNull_throwsNullPointerException() {

		MediumActionComparator comparator = new MediumActionComparator();

		MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
			new MediumRegion(TestMedia.at(0), MediumActionComparatorTest.DEFAULT_BYTES.remaining()), 0,
			MediumActionComparatorTest.DEFAULT_BYTES);

		comparator.compare(actionA1, null);
	}

	/**
	 * @param actionsToAdd the actions to add and check for being contained
	 */
	private void testTreeSetContainsReturnsTrueForAllActions(MediumAction... actionsToAdd) {
		Set<MediumAction> actionSetToSort = new TreeSet<>(new MediumActionComparator());

		for (MediumAction mediumAction : actionsToAdd) {
			actionSetToSort.add(mediumAction);
			Assert.assertTrue(actionSetToSort.contains(mediumAction));
		}

		// Check again after having added all Actions: Still all of them must be
		// contained
		for (MediumAction mediumAction : actionsToAdd) {
			Assert.assertTrue(actionSetToSort.contains(mediumAction));
		}
	}

	/**
	 * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
	 */
	@Test
	public void treeSetContains_forComplexActionSequenceUsingMediumActionComparator_returnsTrueForAllElements() {

		String insertString1 = "===CF9b[11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111]===";

		testTreeSetContainsReturnsTrueForAllActions(
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), insertString1.length()), 0,
				ByteBuffer.wrap(insertString1.getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.REPLACE, new MediumRegion(TestMedia.at(2), 320), 1,
				ByteBuffer.wrap("==".getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.REMOVE, new MediumRegion(TestMedia.at(1), 1), 2, null),
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), 1), 3,
				ByteBuffer.wrap("a".getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), 1), 4,
				ByteBuffer.wrap("b".getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), 1), 5,
				ByteBuffer.wrap("c".getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), 1), 6,
				ByteBuffer.wrap("d".getBytes(Charsets.CHARSET_ASCII))),
			new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(1), 1), 7,
				ByteBuffer.wrap("e".getBytes(Charsets.CHARSET_ASCII))));
	}
}
