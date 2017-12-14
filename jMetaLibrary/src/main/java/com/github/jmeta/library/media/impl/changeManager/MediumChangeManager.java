/**
 *
 * MediumChangeManager.java
 *
 * @author Jens
 *
 * @date 22.05.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.github.jmeta.library.media.api.exceptions.InvalidMediumActionException;
import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.api.types.MediumRegion.MediumRegionOverlapType;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.errors.api.services.JMetaIllegalStateException;

/**
 * {@link MediumChangeManager} performs all tasks of handling and consolidating {@link MediumAction}s.
 * {@link MediumAction}s are created by the methods
 * {@link MediumStore#insertData(com.github.jmeta.library.media.api.types.MediumOffset, ByteBuffer)},
 * {@link MediumStore#removeData(com.github.jmeta.library.media.api.types.MediumOffset, int)} and
 * {@link MediumStore#replaceData(com.github.jmeta.library.media.api.types.MediumOffset, int, ByteBuffer)} by calling
 * one of the corresponding schedule methods of {@link MediumChangeManager}.
 * 
 * {@link MediumAction}s represent an open action that is still to be performed before a {@link MediumStore#flush()}.
 * 
 * {@link MediumChangeManager} ensures that these {@link MediumAction}s are created consistently and maintains them in a
 * specific order such that {@link MediumStore#flush()} can be more easier implemented. It does this by using a
 * {@link TreeSet} as internal data structure and the {@link MediumActionComparator} as sorting criterion.
 */
public class MediumChangeManager {

   private TreeSet<MediumAction> mediumActions = new TreeSet<>(new MediumActionComparator());
   private long nextScheduleSequenceNumber = 0;
   private final MediumOffsetFactory mediumReferenceFactory;
   private static final Set<MediumActionType> EXISTING_ACTION_TYPES_TO_CHECK = new HashSet<>();
   static {
      EXISTING_ACTION_TYPES_TO_CHECK.add(MediumActionType.REMOVE);
      EXISTING_ACTION_TYPES_TO_CHECK.add(MediumActionType.REPLACE);
   }

   /**
    * Creates a new {@link MediumChangeManager}.
    * 
    * @param mediumReferenceFactory
    *           the {@link MediumOffsetFactory} used to create new {@link MediumOffset}s, if necessary.
    */
   public MediumChangeManager(MediumOffsetFactory mediumReferenceFactory) {
      this.mediumReferenceFactory = mediumReferenceFactory;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#REMOVE}. The remove action must not overlap
    * with an already existing remove or replace action. Otherwise this method will throw an
    * {@link InvalidOverlappingWriteException}. If the new {@link MediumAction} entirely contains one or several
    * existing {@link MediumAction}s of any other type (i.e. is equal to it or fully encloses it), the existing
    * {@link MediumAction} is invalidated (i.e. its {@link MediumAction#isPending()} will return false) and removed from
    * the {@link MediumChangeManager}.
    * 
    * The method returns the newly created {@link MediumAction}.
    * 
    * @param removedRegion
    *           The {@link MediumRegion} the remove action refers to, i.e. the start offset and number of bytes to
    *           remove on the external medium.
    * @return The {@link MediumAction} representing the removal. The returned {@link MediumAction} is assigned a
    *         zero-based schedule sequence number that is guaranteed to be bigger than the sequence number of the last
    *         scheduled {@link MediumAction} (no matter what type)
    */
   public MediumAction scheduleRemove(MediumRegion removedRegion) {

      Reject.ifNull(removedRegion, "removedRegion");

      handleExistingInsertsContainedInRegion(removedRegion);

      handleOverlappingExistingRemovesAndReplaces(removedRegion, MediumActionType.REMOVE);

      MediumAction returnedAction = new MediumAction(MediumActionType.REMOVE, removedRegion,
         getAndIncrementNextScheduleSequenceNumber(), null);
      mediumActions.add(returnedAction);

      return returnedAction;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#REPLACE}. The replace action must not overlap
    * with an already existing remove or replace action. Otherwise this method will throw an
    * {@link InvalidOverlappingWriteException}. If the new {@link MediumAction} entirely contains one or several
    * existing {@link MediumAction} of any other type (i.e. is equal to it or fully encloses it), the existing
    * {@link MediumAction} is invalidated (i.e. its {@link MediumAction#isPending()} will return false) and removed from
    * the {@link MediumChangeManager}.
    * 
    * The method returns the newly created {@link MediumAction}.
    * 
    * @param replacedRegion
    *           The {@link MediumRegion} the replace action refers to, i.e. the start offset and number of bytes to
    *           replace on the external medium.
    * @param replacementBytes
    *           The {@link ByteBuffer} containing the replacement bytes between its {@link ByteBuffer#position()} and
    *           {@link ByteBuffer#limit()}
    * @return The {@link MediumAction} representing the replacement. The returned {@link MediumAction} is assigned a
    *         zero-based schedule sequence number that is guaranteed to be bigger than the sequence number of the last
    *         scheduled {@link MediumAction} (no matter what type)
    */
   public MediumAction scheduleReplace(MediumRegion replacedRegion, ByteBuffer replacementBytes) {

      Reject.ifNull(replacedRegion, "replacedRegion");

      handleExistingInsertsContainedInRegion(replacedRegion);

      handleOverlappingExistingRemovesAndReplaces(replacedRegion, MediumActionType.REPLACE);

      MediumAction returnedAction = new MediumAction(MediumActionType.REPLACE, replacedRegion,
         getAndIncrementNextScheduleSequenceNumber(), replacementBytes);
      mediumActions.add(returnedAction);

      return returnedAction;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#INSERT}. If the insert action is contained
    * within the removed or replaced region of already scheduled remove or replace actions, this method throws an
    * {@link InvalidOverlappingWriteException}.
    * 
    * The method returns the newly created {@link MediumAction}.
    * 
    * @param insertionRegion
    *           The {@link MediumRegion} the insert action refers to, i.e. the start offset and number of bytes to
    *           insert on the external medium.
    * @param insertionBytes
    *           The {@link ByteBuffer} containing the bytes to insert between its {@link ByteBuffer#position()} and
    *           {@link ByteBuffer#limit()}
    * @return The {@link MediumAction} representing the insertion. The returned {@link MediumAction} is assigned a
    *         zero-based schedule sequence number that is guaranteed to be bigger than the sequence number of the last
    *         scheduled {@link MediumAction} (no matter what type)
    */
   public MediumAction scheduleInsert(MediumRegion insertionRegion, ByteBuffer insertionBytes) {
      Reject.ifNull(insertionRegion, "insertionRegion");

      MediumAction previousAction = getPreviousAction(insertionRegion);

      verifyExistingRemoveOrReplaceNotContainingInsertOffset(insertionRegion, previousAction);

      MediumAction returnedAction = new MediumAction(MediumActionType.INSERT, insertionRegion,
         getAndIncrementNextScheduleSequenceNumber(), insertionBytes);
      mediumActions.add(returnedAction);

      return returnedAction;
   }

   /**
    * Undoes the given {@link MediumAction} which must have been created previously by a call to
    * {@link #scheduleInsert(MediumRegion, ByteBuffer)}, {@link #scheduleRemove(MediumRegion)} or
    * {@link #scheduleReplace(MediumRegion, ByteBuffer)}. If the {@link MediumAction} specified is unknown, this method
    * throws an {@link InvalidMediumActionException}.
    * 
    * The undone {@link MediumAction} is invalidated by setting it to done, i.e. after undoing the {@link MediumAction},
    * its {@link MediumAction#isPending()} method returns false.
    * 
    * @param action
    *           The {@link MediumAction} to undo
    */
   public void undo(MediumAction action) {
      Reject.ifNull(action, "action");

      if (!mediumActions.contains(action)) {
         throw new InvalidMediumActionException(action);
      }

      action.setDone();

      mediumActions.remove(action);
   }

   /**
    * Creates a flush plan in form a of a list of {@link MediumAction}s to execute by a {@link MediumStore#flush()}. It
    * considers all previously (i.e. since opening the medium or last successful flush) scheduled changes, enriches them
    * with direct {@link MediumActionType#READ}, {@link MediumActionType#WRITE} and {@link MediumActionType#TRUNCATE}
    * actions and ensures they are returned in an order such that no existing medium bytes are incorrectly overridden by
    * changes.
    * 
    * The details of the overall flush algorithm are described at {@link MediumStore#flush()}.
    * 
    * @param maxReadWriteBlockSizeInBytes
    *           The maximum read-write block size in bytes, must be strictly positive
    * @param totalMediumSizeInBytes
    *           The overall number of bytes the medium currently has, must be positive, might be zero
    * @return The flush plan in form a of a list of {@link MediumAction}s to execute by a {@link MediumStore#flush()}
    */
   public List<MediumAction> createFlushPlan(int maxReadWriteBlockSizeInBytes, long totalMediumSizeInBytes) {

      Reject.ifNegativeOrZero(maxReadWriteBlockSizeInBytes, "maxReadWriteBlockSizeInBytes");
      Reject.ifNegative(totalMediumSizeInBytes, "totalMediumSizeInBytes");

      List<MediumAction> flushPlan = new ArrayList<>();
      List<ShiftedMediumBlock> mediumBlocks = new ArrayList<>();

      int delta = 0;

      Iterator<MediumAction> actionIterator = iterator();

      while (actionIterator.hasNext()) {
         MediumAction currentAction = actionIterator.next();
         MediumRegion currentRegion = currentAction.getRegion();

         ShiftedMediumBlock lastBlock = null;
         if (mediumBlocks.size() > 0) {
            lastBlock = mediumBlocks.get(mediumBlocks.size() - 1);
            MediumOffset startReferenceOfFollowUpBytes = lastBlock.getStartReferenceOfFollowUpBytes();

            if (delta == 0) {
               lastBlock.setTotalMediumByteCount(0);
            } else {
               lastBlock
                  .setTotalMediumByteCount(currentRegion.getStartOffset().distanceTo(startReferenceOfFollowUpBytes));
            }
         }

         delta += currentAction.getSizeDelta();

         mediumBlocks.add(new ShiftedMediumBlock(currentAction, delta));
      }

      if (mediumBlocks.size() > 0) {
         ShiftedMediumBlock lastBlock = mediumBlocks.get(mediumBlocks.size() - 1);

         if (delta == 0) {
            lastBlock.setTotalMediumByteCount(0);
         } else {
            long startOfsLast = lastBlock.getStartReferenceOfFollowUpBytes().getAbsoluteMediumOffset();

            lastBlock.setTotalMediumByteCount(totalMediumSizeInBytes - startOfsLast);
         }
      }

      mediumBlocks.sort(new ShiftedMediumBlockComparator());

      for (int i = 0; i < mediumBlocks.size(); ++i) {
         ShiftedMediumBlock currentBlockInSortOrder = mediumBlocks.get(i);

         flushPlan.addAll(currentBlockInSortOrder.computeResultingActions(maxReadWriteBlockSizeInBytes));
      }

      // Add a truncate action to ensure the file is shortened, if necessary
      if (delta < 0) {
         MediumOffset truncateRef = mediumReferenceFactory.createMediumOffset(totalMediumSizeInBytes + delta);

         flushPlan.add(new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(truncateRef, -delta), 0, null));
      }

      return flushPlan;
   }

   /**
    * Returns an {@link Iterator} of all {@link MediumAction}s currently maintained in this {@link MediumChangeManager}.
    * The order the {@link MediumAction}s are returned by this {@link Iterator} follows the order induced by the
    * {@link MediumActionComparator} class.
    * 
    * @return an {@link Iterator} of all {@link MediumAction}s currently maintained in this {@link MediumChangeManager},
    *         sorted according to {@link MediumActionComparator}.
    */
   public Iterator<MediumAction> iterator() {
      return Collections.unmodifiableSet(mediumActions).iterator();
   }

   /**
    * Completely clears this {@link MediumChangeManager} from all previously scheduled {@link MediumAction}s, thus
    * follow up calls to {@link #iterator()} will return an empty iterator.
    */
   public void clearAll() {
      mediumActions.clear();
   }

   /**
    * @return the next not-yet used schedule sequence number
    */
   private long getAndIncrementNextScheduleSequenceNumber() {

      if (nextScheduleSequenceNumber == Long.MAX_VALUE) {
         throw new JMetaIllegalStateException(
            "You are definitely working too much on the medium, you should sit back, calm down, "
               + "close and reopen it to make further changes",
            null);
      }

      return nextScheduleSequenceNumber++;
   }

   /**
    * Gets the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}, according to the
    * {@link MediumActionComparator}, or null if there is none. According to the definition of
    * {@link MediumActionComparator}, the previous {@link MediumAction} with a smaller or equal {@link MediumOffset} and
    * smaller or equal sequence number will be returned by this method.
    * 
    * Due to the {@link MediumActionComparator} implementation, an existing action whose region starts at the same
    * offset as the new region and overlaps the new region at front, but with smaller length, is returned by this method
    * as previous action, NOT by {@link #getNextAction(MediumRegion)} as nextAction.
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @return the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}
    */
   private MediumAction getPreviousAction(MediumRegion newRegion) {
      // NOTE: The MediumActionType does not matter for the biggest possible action, only the offset and the
      // schedule sequence number (due to the comparator implementation).
      MediumAction biggestPossibleAction = new MediumAction(MediumActionType.READ, newRegion, Integer.MAX_VALUE, null);

      return mediumActions.floor(biggestPossibleAction);
   }

   /**
    * Gets the next {@link MediumAction} already scheduled in this {@link MediumChangeManager}, according to the
    * {@link MediumActionComparator}, or null if there is none. According to the definition of
    * {@link MediumActionComparator}, the next {@link MediumAction} with a bigger or equal {@link MediumOffset} and
    * bigger or equal sequence number will be returned by this method. This method also returns any {@link MediumAction}
    * that has equal {@link MediumOffset} and equal sequence number, but differs in any other attribute.
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @return the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}
    */
   private MediumAction getNextAction(MediumRegion newRegion) {
      // NOTE: The MediumActionType does not matter for the biggest possible action, only the offset and the
      // schedule sequence number (due to the comparator implementation).
      MediumAction biggestPossibleAction = new MediumAction(MediumActionType.READ, newRegion, Integer.MAX_VALUE, null);

      return mediumActions.ceiling(biggestPossibleAction);
   }

   /**
    * Checks and possibly invalidates any existing (already scheduled) {@link MediumAction}s of types
    * {@link MediumActionType#REMOVE} or {@link MediumActionType#REPLACE} against the {@link MediumAction} to be newly
    * created, identified by its {@link MediumActionType} and its {@link MediumRegion}.
    * 
    * This method basically ensures that {@link MediumActionType#REMOVE} or {@link MediumActionType#REPLACE} do not
    * overlap each other, and if they do, either throw an exception or undo the previous action, depending on the
    * specific scenario.
    * 
    * This implements the following design decisions from the design concept:
    * <ul>
    * <li>DD 082, cases 2, 3, 4 and 5</li>
    * <li>DD 083, cases 3, 4, 5, 6, 7, 8, 9, 10</li>
    * <li>DD 084, cases 2, 3, 4, 5</li>
    * </ul>
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @param newActionType
    *           The {@link MediumActionType} of the new {@link MediumAction} to be scheduled, either
    *           {@link MediumActionType#REMOVE} or {@link MediumActionType#REPLACE}.
    */
   private void handleOverlappingExistingRemovesAndReplaces(MediumRegion newRegion, MediumActionType newActionType) {

      Reject.ifFalse(EXISTING_ACTION_TYPES_TO_CHECK.contains(newActionType),
         "actionTypesToHandle.contains(newActionType)");

      // NOTE: The previousAction may be one of the following:
      // - null, if there is no action that is smaller according to the MediumActionComparator OR
      // - the existing action with next smaller MediumReference as the newRegion OR
      // - the existing action with next identical MediumReference as the newRegion
      MediumAction previousAction = getPreviousAction(newRegion);

      while (previousAction != null && EXISTING_ACTION_TYPES_TO_CHECK.contains(previousAction.getActionType())) {

         MediumRegion previousRegion = previousAction.getRegion();

         MediumRegionOverlapType overlapType = MediumRegion.determineRegionOverlap(newRegion, previousRegion);

         if (overlapType == MediumRegionOverlapType.RIGHT_FULLY_INSIDE_LEFT
            || overlapType == MediumRegionOverlapType.SAME_RANGE) {
            // Case 1: Existing region is fully contained in the removed or replaced region (second part of this case
            // see in block of next action)
            undo(previousAction);

            previousAction = getPreviousAction(newRegion);
         } else if (overlapType == MediumRegionOverlapType.LEFT_FULLY_INSIDE_RIGHT) {
            // Case 2: New region is fully contained in an existing region
            throw new InvalidOverlappingWriteException(previousAction, newActionType, newRegion);
         } else if (overlapType == MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_BACK) {
            // Case 3: New region overlaps an existing region at its back
            throw new InvalidOverlappingWriteException(previousAction, newActionType, newRegion);
         } else {
            // The previous region is not enclosed or overlapping, but simply has smaller end offset than new region
            break;
         }
      }

      MediumAction nextAction = getNextAction(newRegion);

      while (nextAction != null && EXISTING_ACTION_TYPES_TO_CHECK.contains(nextAction.getActionType())
         && !nextAction.equals(previousAction)) {

         MediumRegion nextRegion = nextAction.getRegion();

         MediumRegionOverlapType overlapType = MediumRegion.determineRegionOverlap(newRegion, nextRegion);

         if (overlapType == MediumRegionOverlapType.RIGHT_FULLY_INSIDE_LEFT
            || overlapType == MediumRegionOverlapType.SAME_RANGE) {
            // Case 1: Existing region is fully contained in the new region (first part of this case see
            // in block of previous action)
            undo(nextAction);

            nextAction = getNextAction(newRegion);
         } else if (overlapType == MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_FRONT) {
            // Case 4: New region overlaps an existing region at its front
            throw new InvalidOverlappingWriteException(nextAction, newActionType, newRegion);
         } else {
            // The next region is not enclosed or overlapping, but simply has bigger start offset than end of new
            // region
            break;
         }
      }
   }

   /**
    * Checks if any existing {@link MediumAction} with type {@link MediumActionType#INSERT} have an insertion offset
    * contained in the new region passed. If so, they are undone.
    * 
    * This implements the following design decisions from the design concept:
    * <ul>
    * <li>DD 079, case 5</li>
    * <li>DD 080, case 5</li>
    * </ul>
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    */
   private void handleExistingInsertsContainedInRegion(MediumRegion newRegion) {
      MediumAction nextAction = null;

      // Note that getNextAction() will NOT return a INSERTs at the same offset as the new region start offset
      while ((nextAction = getNextAction(newRegion)) != null
         && newRegion.contains(nextAction.getRegion().getStartOffset())) {
         if (nextAction.getActionType() == MediumActionType.INSERT) {
            undo(nextAction);
         } else {
            newRegion = nextAction.getRegion();
         }
      }
   }

   /**
    * Checks if any existing {@link MediumAction} with type {@link MediumActionType#REMOVE} or
    * {@link MediumActionType#REPLACE} have a removed or replaced region containing the start offset of the given
    * region. If so, an {@link InvalidOverlappingWriteException} is thrown.
    * 
    * This implements the following design decisions from the design concept:
    * <ul>
    * <li>DD 079, case 6</li>
    * <li>DD 080, case 6</li>
    * </ul>
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @param previousAction
    *           The previous {@link MediumAction}, might be null
    */
   private void verifyExistingRemoveOrReplaceNotContainingInsertOffset(MediumRegion newRegion,
      MediumAction previousAction) {
      // Note that getPreviousAction() will also return a REPLACE or REMOVE at the same offset as the insert
      if (previousAction != null && EXISTING_ACTION_TYPES_TO_CHECK.contains(previousAction.getActionType())) {
         MediumRegion previousRegion = previousAction.getRegion();

         if (previousRegion.contains(newRegion.getStartOffset())
            && !newRegion.getStartOffset().equals(previousRegion.getStartOffset())) {
            throw new InvalidOverlappingWriteException(previousAction, previousAction.getActionType(), newRegion);
         }
      }
   }
}
