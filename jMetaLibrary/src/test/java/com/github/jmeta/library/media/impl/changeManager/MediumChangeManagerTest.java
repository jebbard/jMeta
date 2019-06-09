/**
 * {@link MediumChangeManagerTest}.java
 *
 * @author Jens
 * @date 22.05.2016
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.InvalidMediumActionException;
import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;

/**
 * {@link MediumChangeManagerTest} tests the {@link MediumChangeManager} class
 * except the tests of the
 * {@link MediumChangeManager#createFlushPlan(int, long)} method which can be
 * found in the {@link MediumChangeManagerCreateFlushPlanTest} class.
 *
 * Test methods for
 * {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} follow
 * a specific naming pattern:
 *
 * [design decision][case]_[schedule calls]_[offset properties]_[expectation]
 *
 * where [design decision] and [case] come from "EasyTag_DesignConcept.pdf".
 */
public class MediumChangeManagerTest {

	private static final ByteBuffer BUFFER_SIZE_5 = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
	private static final ByteBuffer BUFFER_SIZE_10_A = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });
	private static final ByteBuffer BUFFER_SIZE_10_B = ByteBuffer.wrap(new byte[] { 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 });
	private static final ByteBuffer BUFFER_SIZE_20 = ByteBuffer
		.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
	private static final ByteBuffer BUFFER_SIZE_40 = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });

	/**
	 * Performs the actual call to the correct {@link MediumChangeManager} schedule
	 * method based on its type.
	 * 
	 * @param testling    The {@link MediumChangeManager} under test
	 * @param testDataSet The {@link MediumChangeManagerTestData} containing the
	 *                    input data for the call.
	 * @return The {@link MediumAction} resulting from the schedule call.
	 */
	private MediumAction callScheduleMethod(MediumChangeManager testling, MediumChangeManagerTestData testDataSet) {
		switch (testDataSet.getTypeToUse()) {
		case INSERT:
			return testling.scheduleInsert(testDataSet.getRegionToUse(), testDataSet.getActionBytesToUse());
		case REMOVE:
			return testling.scheduleRemove(testDataSet.getRegionToUse());
		case REPLACE:
			return testling.scheduleReplace(testDataSet.getRegionToUse(), testDataSet.getActionBytesToUse());
		default:
			throw new IllegalArgumentException("Only REPLACE, REMOVE and INSERT are supported");
		}
	}

	/**
	 * Checks that an {@link InvalidOverlappingWriteException} is thrown when
	 * performing a schedule call with the provided
	 * {@link MediumChangeManagerTestData}.
	 * 
	 * @param testling The {@link MediumChangeManager} instance under test.
	 * @param testData one or more {@link MediumChangeManagerTestData} sets
	 *                 containing input and expected data for scheduling a new
	 *                 {@link MediumAction} using {@link MediumChangeManager}.
	 */
	private void checkExceptionOnSchedule(MediumChangeManager testling, MediumChangeManagerTestData... testData) {

		for (MediumChangeManagerTestData testDataSet : testData) {
			try {
				callScheduleMethod(testling, testDataSet);

				Assert.fail("Expected exception of type " + InvalidOverlappingWriteException.class);
			} catch (InvalidOverlappingWriteException e) {
				Assert.assertTrue("Exception of type " + InvalidOverlappingWriteException.class + ", as expected: " + e,
					true);
			}
		}
	}

	/**
	 * Checks that the {@link MediumChangeManager#iterator()} method returns
	 * {@link MediumAction}s in the expected order.
	 * 
	 * @param expectedMediumActions        The {@link MediumAction}s in their
	 *                                     expected order. All indices of other
	 *                                     parameters of this method refer to this
	 *                                     {@link List}.
	 * @param iteratorToCheck              The {@link Iterator} to check for
	 *                                     returning the expected
	 *                                     {@link MediumAction}s in the specified
	 *                                     order.
	 * @param expectedActionIndicesInOrder This array contains all the indices of
	 *                                     {@link MediumAction}s in the list of
	 *                                     scheduled actions in their expected
	 *                                     order.
	 * @param expectedDoneActionIndices    Contains all indices of
	 *                                     {@link MediumAction}s that are expected
	 *                                     to set to done (i.e.
	 *                                     {@link MediumAction#isPending()} returns
	 *                                     false). All other {@link MediumAction}s
	 *                                     are expected to be still pending.
	 */
	private void checkIteratorAgainstScheduledActions(List<MediumAction> expectedMediumActions,
		Iterator<MediumAction> iteratorToCheck, int[] expectedActionIndicesInOrder,
		List<Integer> expectedDoneActionIndices) {

		for (int i : expectedActionIndicesInOrder) {
			Assert.assertTrue(iteratorToCheck.hasNext());
			Assert.assertEquals(expectedMediumActions.get(i), iteratorToCheck.next());
		}

		for (int i = 0; i < expectedMediumActions.size(); i++) {

			if (expectedDoneActionIndices.contains(i)) {
				Assert.assertFalse(expectedMediumActions.get(i).isPending());
			} else {
				Assert.assertTrue(expectedMediumActions.get(i).isPending());
			}
		}

		Assert.assertFalse(iteratorToCheck.hasNext());
	}

	/**
	 * Performs the call to the schedule methods of {@link MediumChangeManager},
	 * using the given test data sets.
	 * 
	 * @param testling The {@link MediumChangeManager} under test
	 * @param testData The test data sets to use as input and verification when
	 *                 calling the scheduling methods.
	 * @return A {@link List} of scheduled {@link MediumAction}s - one per
	 *         {@link MediumChangeManagerTestData} set given - in their scheduling
	 *         order, i.e. same order as the given
	 *         {@link MediumChangeManagerTestData}s.
	 */
	private List<MediumAction> checkScheduleForRegions(MediumChangeManager testling,
		MediumChangeManagerTestData... testData) {

		List<MediumAction> expectedActionsInIteratorOrder = new ArrayList<>();

		int nextSequenceNumber = 0;

		for (MediumChangeManagerTestData testDataSet : testData) {
			MediumRegion mediumRegion = testDataSet.getRegionToUse();

			MediumAction returnedAction = callScheduleMethod(testling, testDataSet);

			MediumAction expectedAction = new MediumAction(testDataSet.getTypeToUse(), mediumRegion,
				nextSequenceNumber++, testDataSet.getActionBytesToUse());

			Assert.assertEquals(expectedAction, returnedAction);

			expectedActionsInIteratorOrder.add(returnedAction);
		}

		return expectedActionsInIteratorOrder;
	}

	/**
	 * Tests {@link MediumChangeManager#clearAll()}.
	 */
	@Test
	public void clearAll_forEmptyChangeManager_changesNothing() {

		MediumChangeManager testling = getTestling();

		Assert.assertFalse(testling.iterator().hasNext());

		testling.clearAll();

		Assert.assertFalse(testling.iterator().hasNext());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd078case1_scheduleInsertThenInsert_differentOffsets_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(5), MediumChangeManagerTest.BUFFER_SIZE_5.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(20), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), MediumChangeManagerTest.BUFFER_SIZE_10_B.remaining());
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), MediumChangeManagerTest.BUFFER_SIZE_20.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region4,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3, 0, 1, 2, 4 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd078case2_scheduleInsertThenInsert_sameOffset_iteratedUnchangedInScheduleOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_B.remaining()); // Identical
																														// with
																														// region
																														// 0
		MediumRegion region2 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_5.remaining()); // Starts
																														// with
																														// region
																														// 0
																														// &
																														// 1,
																														// but
																														// ends
		// earlier
		MediumRegion region3 = new MediumRegion(TestMedia.at(20), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining()); // No
																															// relation
																															// to
																															// any
																															// other
																															// region
		MediumRegion region4 = new MediumRegion(TestMedia.at(2), MediumChangeManagerTest.BUFFER_SIZE_40.remaining()); // Overlapping
																														// all
																														// other
																														// regions,
		// includes region 3

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region4,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1, 2, 4, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case1_scheduleInsertThenRemove_removedRangeDoesNotContainInsertOffset_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(12), 5);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100),
			MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(20), 55);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1, 3, 2 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case2_scheduleRemoveThenInsert_removedRangeDoesNotContainInsertOffset_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(12), 5);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(20), 55);
		MediumRegion region3 = new MediumRegion(TestMedia.at(100),
			MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 0, 2, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case3_scheduleInsertThenRemove_sameOffset_iteratedUnchangedInScheduleOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), 5);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case4_scheduleRemoveThenInsert_sameOffset_iteratedReturnsInsertBeforeRenove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), 5);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case5_scheduleInsertThenRemove_removedRangeContainsInsertOffset_iteratorOnlyReturnsRemove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 5);
		MediumRegion region2 = new MediumRegion(TestMedia.at(200),
			MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(187), 100);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 3 },
			Arrays.asList(0, 2));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case5_scheduleInsertThenRemove_removedRangeContainsMultipleInsertOffsets_iteratorOnlyReturnsRemove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(2), MediumChangeManagerTest.BUFFER_SIZE_5.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(2), MediumChangeManagerTest.BUFFER_SIZE_40.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), 43);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3 },
			Arrays.asList(0, 1, 2));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd079case6_scheduleRemoveThenInsert_removedRangeContainsInsertOffset_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(0), 5);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(199), 5);
		MediumRegion region3 = new MediumRegion(TestMedia.at(203), MediumChangeManagerTest.BUFFER_SIZE_40.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case1_scheduleInsertThenReplace_replacedRangeDoesNotContainInsertOffset_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(2), 5);
		MediumRegion region2 = new MediumRegion(TestMedia.at(20), MediumChangeManagerTest.BUFFER_SIZE_20.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(45), MediumChangeManagerTest.BUFFER_SIZE_40.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1, 2, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case2_scheduleReplaceThenInsert_replacedRangeDoesNotContainInsertOffset_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(2), 5);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(45), MediumChangeManagerTest.BUFFER_SIZE_20.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(65), MediumChangeManagerTest.BUFFER_SIZE_20.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 0, 2, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case3_scheduleInsertThenReplace_sameOffset_iteratedUnchangedInScheduleOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), 5);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case4_scheduleReplaceThenInsert_sameOffset_iteratorReturnsInsertBeforeReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), 5);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case5_scheduleInsertThenReplace_replacedRangeContainsInsertOffset_iteratorOnlyReturnsReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), 20);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1 }, Arrays.asList(0));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case5_scheduleInsertThenReplace_replacedRangeContainsMultipleInsertOffsets_iteratorOnlyReturnsReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region1 = new MediumRegion(TestMedia.at(2), MediumChangeManagerTest.BUFFER_SIZE_5.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(20), MediumChangeManagerTest.BUFFER_SIZE_40.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(2), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region4 = new MediumRegion(TestMedia.at(1), 20);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 4 },
			Arrays.asList(0, 1, 2, 3));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd080case6_scheduleReplaceThenInsert_replacedRangeContainsInsertOffset_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1), 20);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), MediumChangeManagerTest.BUFFER_SIZE_10_A.remaining());
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), MediumChangeManagerTest.BUFFER_SIZE_20.remaining());
		MediumRegion region3 = new MediumRegion(TestMedia.at(101), MediumChangeManagerTest.BUFFER_SIZE_5.remaining());
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), MediumChangeManagerTest.BUFFER_SIZE_5.remaining());
		MediumRegion region5 = new MediumRegion(
			TestMedia.at((200 + MediumChangeManagerTest.BUFFER_SIZE_5.remaining()) - 1),
			MediumChangeManagerTest.BUFFER_SIZE_20.remaining());

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region5,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1, 2 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case1_scheduleRemoveThenRemove_differentOffsetsWithoutOverlaps_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(11), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(35), 5);
		MediumRegion region2 = new MediumRegion(TestMedia.at(220), 10);
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region4 = new MediumRegion(TestMedia.at(2000), 1110);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3, 0, 1, 2, 4 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case2_scheduleRemoveThenRemove_laterRegionFullyEnclosesMultiplePreviousRegions_iteratorOnlyReturnsLaterRemove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), 9);
		MediumRegion region2 = new MediumRegion(TestMedia.at(31), 40);
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), 100); // Completely encloses all other regions

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3 },
			Arrays.asList(0, 1, 2));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case2_scheduleRemoveThenRemove_secondRegionFullyEnclosesFirst_iteratorOnlyReturnsSecondRemove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(10), 10); // Identical to region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(30), 100); // No relation to any other region
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), 10); // Completely enclosed by region 7
		MediumRegion region5 = new MediumRegion(TestMedia.at(2000), 10); // Completely enclosed by region 6
		MediumRegion region6 = new MediumRegion(TestMedia.at(2000), 100); // Starts at same offset as region 5, but
																			// longer
		MediumRegion region7 = new MediumRegion(TestMedia.at(190), 100); // Completely encloses region 4
		MediumRegion region8 = new MediumRegion(TestMedia.at(3000), 100);
		MediumRegion region9 = new MediumRegion(TestMedia.at(2900), 200); // Completely encloses region 8

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region6, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region7, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region8, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region9, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 3, 7, 6, 9 },
			Arrays.asList(0, 4, 5, 8));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case3_scheduleRemoveThenRemove_firstRegionFullyEnclosesSecond_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region2 = new MediumRegion(TestMedia.at(11), 8); // Completely enclosed by region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(118), 1); // Completely enclosed by region 1

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList(2, 3));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case4_scheduleRemoveThenRemove_secondRegionOverlapsFirstAtBack_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(119), 2); // Overlaps region 2 at end by 1 byte
		MediumRegion region4 = new MediumRegion(TestMedia.at(11), 9); // Overlaps region 1 at end, reaches until end of
																		// region 1
		MediumRegion region5 = new MediumRegion(TestMedia.at(1100), 250); // Overlaps region 0 at end by 100 bytes

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd082case5_scheduleRemoveThenRemove_secondRegionOverlapsFirstAtFront_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(90), 29); // Overlaps region 2 at start by 19 bytes
		MediumRegion region4 = new MediumRegion(TestMedia.at(9), 2); // Overlaps region 1 at start by 1 byte
		MediumRegion region5 = new MediumRegion(TestMedia.at(900), 250); // Overlaps region 0 at start by 150 bytes
		MediumRegion region6 = new MediumRegion(TestMedia.at(1200), 70);
		MediumRegion region7 = new MediumRegion(TestMedia.at(1200), 40); // Starts at same offset as region 6, overlaps
																			// it by 40 b.

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region6, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region7, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case1_scheduleRemoveThenReplace_differentOffsetsWithoutOverlaps_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(11), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 10); // Only 10 bytes replaced
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 10);
		MediumRegion region3 = new MediumRegion(TestMedia.at(90), 10); // 10 bytes to replace

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			// But replacement region overlaps 30 Bytes at front
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			// 10 new bytes
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 0, 3, 2 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case10_scheduleReplaceThenRemove_secondRegionOverlapsFirstAtFront_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(90), 29); // Overlaps region 2 at start by 19 bytes
		MediumRegion region4 = new MediumRegion(TestMedia.at(9), 2); // Overlaps region 1 at start by 1 byte
		MediumRegion region5 = new MediumRegion(TestMedia.at(900), 250); // Overlaps region 0 at start by 150 bytes
		MediumRegion region6 = new MediumRegion(TestMedia.at(1200), 70);
		MediumRegion region7 = new MediumRegion(TestMedia.at(1200), 40); // Starts at same offs. as reg. 6, overl. 40b.

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region6,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region7, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case2_scheduleReplaceThenRemove_differentOffsetsWithoutOverlaps_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(11), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 10);
		MediumRegion region3 = new MediumRegion(TestMedia.at(110), 10);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1, 2, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case3_scheduleRemoveThenReplace_laterRegionFullyEnclosesMultiplePreviousRegions_iteratorOnlyReturnsLaterReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(1), 9);
		MediumRegion region2 = new MediumRegion(TestMedia.at(31), 40);
		MediumRegion region3 = new MediumRegion(TestMedia.at(1), 70); // Completely encloses all other regions

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3 },
			Arrays.asList(0, 1, 2));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case3_scheduleRemoveThenReplace_secondRegionFullyEnclosesFirst_iteratorOnlyReturnsSecondReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(10), 10); // Identical to region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(30), 100); // No relation to any other region
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), 10); // Completely enclosed by region 7
		MediumRegion region5 = new MediumRegion(TestMedia.at(2000), 10); // Completely enclosed by region 6
		MediumRegion region6 = new MediumRegion(TestMedia.at(2000), 100); // Starts at same offs. as reg. 5, but longer
		MediumRegion region7 = new MediumRegion(TestMedia.at(190), 100); // Completely encloses region 4
		MediumRegion region8 = new MediumRegion(TestMedia.at(3000), 100);
		MediumRegion region9 = new MediumRegion(TestMedia.at(2900), 200); // Completely encloses region 8

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region6,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region7,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region8, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region9,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 3, 7, 6, 9 },
			Arrays.asList(0, 4, 5, 8));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case4_scheduleReplaceThenRemove_secondRegionFullyEnclosesFirst_iteratorOnlyReturnsSecondRemove() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(10), 10); // Identical to region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(30), 100); // No relation to any other region
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), 10); // Completely enclosed by region 7
		MediumRegion region5 = new MediumRegion(TestMedia.at(2000), 10); // Completely enclosed by region 6
		MediumRegion region6 = new MediumRegion(TestMedia.at(2000), 100); // Starts at same offset as region 5, but
																			// longer
		MediumRegion region7 = new MediumRegion(TestMedia.at(190), 100); // Completely encloses region 4
		MediumRegion region8 = new MediumRegion(TestMedia.at(3000), 100);
		MediumRegion region9 = new MediumRegion(TestMedia.at(2900), 200); // Completely encloses region 8

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region6, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region7, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region8,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region9, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 3, 7, 6, 9 },
			Arrays.asList(0, 4, 5, 8));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case5_scheduleRemoveThenReplace_firstRegionFullyEnclosesSecond_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region2 = new MediumRegion(TestMedia.at(11), 8); // Completely enclosed by region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(118), 1); // Completely enclosed by region 1

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case6_scheduleReplaceThenRemove_firstRegionFullyEnclosesSecond_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region2 = new MediumRegion(TestMedia.at(11), 8); // Completely enclosed by region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(118), 1); // Completely enclosed by region 1

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case7_scheduleRemoveThenReplace_secondRegionOverlapsFirstAtBack_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(119), 2); // Overlaps region 2 at end by 1 byte
		MediumRegion region4 = new MediumRegion(TestMedia.at(11), 9); // Overlaps reg. 1 at end, reaches to eor 1
		MediumRegion region5 = new MediumRegion(TestMedia.at(1100), 250); // Overlaps region 0 at end by 100 bytes

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case8_scheduleReplaceThenRemove_secondRegionOverlapsFirstAtBack_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(119), 2); // Overlaps region 2 at end by 1 byte
		MediumRegion region4 = new MediumRegion(TestMedia.at(11), 9); // Overlaps reg. 1 at end, reaches to eor 1
		MediumRegion region5 = new MediumRegion(TestMedia.at(1100), 250); // Overlaps region 0 at end by 100 bytes

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region4, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region5, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd083case9_scheduleRemoveThenReplace_secondRegionOverlapsFirstAtFront_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(90), 29); // Overlaps region 2 at start by 19 bytes
		MediumRegion region4 = new MediumRegion(TestMedia.at(9), 2); // Overlaps region 1 at start by 1 byte
		MediumRegion region5 = new MediumRegion(TestMedia.at(900), 250); // Overlaps region 0 at start by 150 bytes
		MediumRegion region6 = new MediumRegion(TestMedia.at(1200), 70);
		MediumRegion region7 = new MediumRegion(TestMedia.at(1200), 40); // Starts at same offs. as reg. 6, overl. 40b.

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region0, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region6, null));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region7,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0, 3 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd084case1_scheduleReplaceThenReplace_differentOffsetsWithoutOverlaps_iteratedUnchangedInOffsetOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(11), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(35), 5);
		MediumRegion region2 = new MediumRegion(TestMedia.at(220), 5);
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region4 = new MediumRegion(TestMedia.at(2000), 1110);
		MediumRegion region5 = new MediumRegion(TestMedia.at(225), 10);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3, 0, 1, 2, 5, 4 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)} and
	 * {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd084case2_scheduleReplaceThenReplace_secondRegionFullyEnclosesFirst_iteratorOnlyReturnsSecondReplace() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(10), 10); // Identical to region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(30), 100); // No relation to any other region
		MediumRegion region4 = new MediumRegion(TestMedia.at(200), 10); // Completely enclosed by region 7
		MediumRegion region5 = new MediumRegion(TestMedia.at(2000), 10); // Completely enclosed by region 6
		MediumRegion region6 = new MediumRegion(TestMedia.at(2000), 100); // Starts at same offset as region 5, but
																			// longer
		MediumRegion region7 = new MediumRegion(TestMedia.at(190), 100); // Completely encloses region 4
		MediumRegion region8 = new MediumRegion(TestMedia.at(3000), 100);
		MediumRegion region9 = new MediumRegion(TestMedia.at(2900), 200); // Completely encloses region 8

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region6,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region7,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region8,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region9,
				MediumChangeManagerTest.BUFFER_SIZE_10_A));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 3, 7, 6, 9 },
			Arrays.asList(0, 4, 5, 8));
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd084case3_scheduleReplaceThenReplace_firstRegionFullyEnclosesSecond_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region1 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region2 = new MediumRegion(TestMedia.at(11), 8); // Completely enclosed by region 0
		MediumRegion region3 = new MediumRegion(TestMedia.at(118), 1); // Completely enclosed by region 1

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region3, null));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 0, 1 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd084case4_scheduleReplaceThenReplace_secondRegionOverlapsFirstAtBack_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(119), 2); // Overlaps region 2 at end by 1 byte
		MediumRegion region4 = new MediumRegion(TestMedia.at(11), 9); // Overlaps reg. 1 at end, reaches to eor 1
		MediumRegion region5 = new MediumRegion(TestMedia.at(1100), 250); // Overlaps region 0 at end by 100 bytes

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_5));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0 },
			Arrays.asList());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)}
	 * and {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void dd084case5_scheduleReplaceThenReplace_secondRegionOverlapsFirstAtFront_throwsException() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 200);
		MediumRegion region1 = new MediumRegion(TestMedia.at(10), 10);
		MediumRegion region2 = new MediumRegion(TestMedia.at(100), 20);
		MediumRegion region3 = new MediumRegion(TestMedia.at(90), 29); // Overlaps region 2 at start by 19 bytes
		MediumRegion region4 = new MediumRegion(TestMedia.at(9), 2); // Overlaps region 1 at start by 1 byte
		MediumRegion region5 = new MediumRegion(TestMedia.at(900), 250); // Overlaps region 0 at start by 150 bytes
		MediumRegion region6 = new MediumRegion(TestMedia.at(1200), 70);
		MediumRegion region7 = new MediumRegion(TestMedia.at(1200), 40); // Starts at same offs. as reg. 6, overl. 40b.

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region0,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region1,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region2,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region6,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkExceptionOnSchedule(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region3,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_10_A),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region7,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2, 0, 3 },
			Arrays.asList());
	}

	/**
	 * @return an instance of a new {@link MediumChangeManager} for testing
	 */
	private MediumChangeManager getTestling() {
		return new MediumChangeManager(new MediumOffsetFactory(TestMedia.DEFAULT_TEST_MEDIUM));
	}

	/**
	 * Tests {@link MediumChangeManager#iterator()}.
	 */
	@Test
	public void iterator_emptyChangeManger_returnsNothing() {

		MediumChangeManager testling = getTestling();

		Assert.assertFalse(testling.iterator().hasNext());
	}

	/**
	 * Tests {@link MediumChangeManager#scheduleRemove(MediumRegion)},
	 * {@link MediumChangeManager#scheduleReplace(MediumRegion, ByteBuffer)},
	 * {@link MediumChangeManager#scheduleInsert(MediumRegion, ByteBuffer)} and
	 * {@link MediumChangeManager#iterator()} in a complex call sequence that has no
	 * overlaps (except some previous regions in later added ones).
	 */
	@Test
	public void schedule_complexCaseNoOverlaps_iteratorReturnsActionsInExpectedOrder() {

		MediumChangeManager testling = getTestling();

		// Regions are listed in order of scheduling
		MediumRegion region0 = new MediumRegion(TestMedia.at(1000), 40);
		MediumRegion region1 = new MediumRegion(TestMedia.at(0), 100); // Completely enclosed by region1 and thus
																		// ignored
		MediumRegion region2 = new MediumRegion(TestMedia.at(0), 188);
		MediumRegion region3 = new MediumRegion(TestMedia.at(0), 20);
		MediumRegion region4 = new MediumRegion(TestMedia.at(188), 600); // Completely enclosed by region5 and thus
																			// ignored
		MediumRegion region5 = new MediumRegion(TestMedia.at(188), 700);
		MediumRegion region6 = new MediumRegion(TestMedia.at(2000), 100);// Completely enclosed by region7 and thus
																			// ignored
		MediumRegion region7 = new MediumRegion(TestMedia.at(1900), 300);
		MediumRegion region8 = new MediumRegion(TestMedia.at(188), 40);
		MediumRegion region9 = new MediumRegion(TestMedia.at(188), 20);
		MediumRegion region10 = new MediumRegion(TestMedia.at(188), 5);
		MediumRegion region11 = new MediumRegion(TestMedia.at(1300), 20);// Completely enclosed by region12 and thus
																			// ignored
		MediumRegion region12 = new MediumRegion(TestMedia.at(1299), 40);
		MediumRegion region13 = new MediumRegion(TestMedia.at(1200), 20);

		List<MediumAction> scheduledActions = checkScheduleForRegions(testling,
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region0,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region1, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region2, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region3,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region4,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region5,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region6,
				MediumChangeManagerTest.BUFFER_SIZE_10_B),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region7, null),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region8,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region9,
				MediumChangeManagerTest.BUFFER_SIZE_20),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region10,
				MediumChangeManagerTest.BUFFER_SIZE_5),
			new MediumChangeManagerTestData(testling, MediumActionType.REMOVE, region11, null),
			new MediumChangeManagerTestData(testling, MediumActionType.REPLACE, region12,
				MediumChangeManagerTest.BUFFER_SIZE_40),
			new MediumChangeManagerTestData(testling, MediumActionType.INSERT, region13,
				MediumChangeManagerTest.BUFFER_SIZE_20));

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(),
			new int[] { 3, 2, 8, 9, 10, 5, 0, 13, 12, 7 }, Arrays.asList(1, 4, 6, 11));
	}

	/**
	 * Tests {@link MediumChangeManager#undo(MediumAction)}.
	 */
	@Test(expected = InvalidMediumActionException.class)
	public void undo_forAlreadyInvalidatedAction_throwsException() {

		MediumChangeManager testling = getTestling();

		testling.scheduleRemove(new MediumRegion(TestMedia.at(12), 20));
		MediumAction act1 = testling.scheduleRemove(new MediumRegion(TestMedia.at(12), 41));
		testling.scheduleReplace(new MediumRegion(TestMedia.at(10), 100), MediumChangeManagerTest.BUFFER_SIZE_40); // Invalidates
																													// previous
																													// schedule
																													// call

		testling.undo(act1);
	}

	/**
	 * Tests {@link MediumChangeManager#undo(MediumAction)}.
	 */
	@Test(expected = InvalidMediumActionException.class)
	public void undo_forEmptyChangeManagerAndUnknownAction_throwsException() {

		MediumChangeManager testling = getTestling();

		testling.undo(new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(12), 40), 0,
			MediumChangeManagerTest.BUFFER_SIZE_40));
	}

	/**
	 * Tests {@link MediumChangeManager#undo(MediumAction)}.
	 */
	@Test(expected = InvalidMediumActionException.class)
	public void undo_forFilledChangeManagerAndUnknownAction_throwsException() {

		MediumChangeManager testling = getTestling();

		testling.scheduleInsert(new MediumRegion(TestMedia.at(12), MediumChangeManagerTest.BUFFER_SIZE_20.remaining()),
			MediumChangeManagerTest.BUFFER_SIZE_20);
		testling.scheduleRemove(new MediumRegion(TestMedia.at(12), 41));
		testling.scheduleReplace(new MediumRegion(TestMedia.at(200), 5), MediumChangeManagerTest.BUFFER_SIZE_40);

		testling.undo(new MediumAction(MediumActionType.INSERT, new MediumRegion(TestMedia.at(12), 40), 0,
			MediumChangeManagerTest.BUFFER_SIZE_40));
	}

	/**
	 * Tests {@link MediumChangeManager#undo(MediumAction)}.
	 */
	@Test
	public void undo_forKnownAction_invalidatesItAndIteratorDoesNotReturnIt() {

		MediumChangeManager testling = getTestling();

		MediumAction act0 = testling.scheduleInsert(
			new MediumRegion(TestMedia.at(12), MediumChangeManagerTest.BUFFER_SIZE_20.remaining()),
			MediumChangeManagerTest.BUFFER_SIZE_20);
		MediumAction act1 = testling.scheduleRemove(new MediumRegion(TestMedia.at(12), 41));
		MediumAction act2 = testling.scheduleReplace(new MediumRegion(TestMedia.at(200), 5),
			MediumChangeManagerTest.BUFFER_SIZE_40);

		testling.undo(act0);

		Assert.assertFalse(act0.isPending());
		Assert.assertTrue(act1.isPending());
		Assert.assertTrue(act2.isPending());

		List<MediumAction> scheduledActions = new ArrayList<>();

		scheduledActions.add(act0);
		scheduledActions.add(act1);
		scheduledActions.add(act2);

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 1, 2 },
			Arrays.asList(0));
	}

	/**
	 * Tests {@link MediumChangeManager#undo(MediumAction)}.
	 */
	@Test
	public void undo_forMultipleKnownActions_invalidatesAndIteratorDoesNotReturnActions() {

		MediumChangeManager testling = getTestling();

		MediumAction act0 = testling.scheduleInsert(
			new MediumRegion(TestMedia.at(12), MediumChangeManagerTest.BUFFER_SIZE_20.remaining()),
			MediumChangeManagerTest.BUFFER_SIZE_20);
		MediumAction act1 = testling.scheduleRemove(new MediumRegion(TestMedia.at(12), 41));
		MediumAction act2 = testling.scheduleReplace(new MediumRegion(TestMedia.at(200), 5),
			MediumChangeManagerTest.BUFFER_SIZE_40);
		MediumAction act3 = testling.scheduleReplace(new MediumRegion(TestMedia.at(0), 5),
			MediumChangeManagerTest.BUFFER_SIZE_40);
		MediumAction act4 = testling.scheduleInsert(
			new MediumRegion(TestMedia.at(58), MediumChangeManagerTest.BUFFER_SIZE_40.remaining()),
			MediumChangeManagerTest.BUFFER_SIZE_40);
		MediumAction act5 = testling.scheduleRemove(new MediumRegion(TestMedia.at(100), 10));
		MediumAction act6 = testling.scheduleRemove(new MediumRegion(TestMedia.at(110), 10));

		testling.undo(act1);
		testling.undo(act4);
		testling.undo(act5);

		Assert.assertFalse(act1.isPending());
		Assert.assertFalse(act4.isPending());
		Assert.assertFalse(act5.isPending());
		Assert.assertTrue(act0.isPending());
		Assert.assertTrue(act2.isPending());
		Assert.assertTrue(act3.isPending());
		Assert.assertTrue(act6.isPending());

		List<MediumAction> scheduledActions = new ArrayList<>();

		scheduledActions.add(act0);
		scheduledActions.add(act1);
		scheduledActions.add(act2);
		scheduledActions.add(act3);
		scheduledActions.add(act4);
		scheduledActions.add(act5);
		scheduledActions.add(act6);

		checkIteratorAgainstScheduledActions(scheduledActions, testling.iterator(), new int[] { 3, 0, 6, 2 },
			Arrays.asList(1, 4, 5));
	}
}
