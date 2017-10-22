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

import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.exceptions.InvalidMediumActionException;
import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.api.types.MediumRegion.MediumRegionOverlapType;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactory;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumChangeManager} performs all tasks of handling and consolidating {@link MediumAction}s.
 * {@link MediumAction}s are created by the methods
 * {@link IMediumStore_OLD#insertData(com.github.jmeta.library.media.api.types.IMediumReference, ByteBuffer)},
 * {@link IMediumStore_OLD#removeData(com.github.jmeta.library.media.api.types.IMediumReference, int)} and
 * {@link IMediumStore_OLD#replaceData(com.github.jmeta.library.media.api.types.IMediumReference, int, ByteBuffer)} by calling one of the
 * corresponding schedule methods of {@link MediumChangeManager}.
 * 
 * {@link MediumAction}s represent an open action that is still to be performed before a
 * {@link IMediumStore_OLD#flush()}.
 * 
 * {@link MediumChangeManager} ensures that these {@link MediumAction}s are created consistently and maintains them in a
 * specific order such that later {@link IMediumStore_OLD#flush()} can be more easier implemented. It does this by using
 * a {@link TreeSet} as internal data structure and the {@link MediumActionComparator} as sorting criterion.
 */
public class MediumChangeManager {

   private TreeSet<MediumAction> mediumActions = new TreeSet<>(new MediumActionComparator());
   private final MediumReferenceFactory mediumReferenceFactory;

   /**
    * Creates a new {@link MediumChangeManager}.
    * 
    * @param mediumReferenceFactory
    *           the {@link MediumReferenceFactory} used to create new {@link IMediumReference}s, if necessary.
    */
   public MediumChangeManager(MediumReferenceFactory mediumReferenceFactory) {
      this.mediumReferenceFactory = mediumReferenceFactory;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#REMOVE}. The remove action must not overlap
    * with an already existing remove or replace action. Otherwise this method will throw an
    * {@link InvalidOverlappingWriteException}. If the new {@link MediumAction} entirely contains one or several
    * existing {@link MediumAction} (i.e. is equal to it or fully encloses it), the existing {@link MediumAction} is
    * invalidated (i.e. its {@link MediumAction#isPending()} will return false) and removed from the
    * {@link MediumChangeManager}.
    * 
    * The method returns the newly created {@link MediumAction}.
    * 
    * @param removedRegion
    *           The {@link MediumRegion} the remove action refers to, i.e. the start offset and number of bytes to
    *           remove on the external medium.
    * @return The {@link MediumAction} representing the removal. The returned {@link MediumAction} is assigned a
    *         zero-based sequence number that is guaranteed to be bigger than the sequence number of the last scheduled
    *         {@link MediumAction} (no matter what type) for the same {@link IMediumReference}, of 0 if there was no
    *         previous {@link MediumAction} scheduled at the {@link IMediumReference} of the given {@link MediumRegion}.
    */
   public MediumAction scheduleRemove(MediumRegion removedRegion) {

      Reject.ifNull(removedRegion, "removedRegion");

      Set<MediumActionType> typeSet = new HashSet<>();

      typeSet.add(MediumActionType.REMOVE);
      typeSet.add(MediumActionType.REPLACE);

      int nextSequenceNumber = handleExistingActionsOfTypes(typeSet, removedRegion, MediumActionType.REMOVE);

      MediumAction returnedAction = new MediumAction(MediumActionType.REMOVE, removedRegion, nextSequenceNumber, null);
      mediumActions.add(returnedAction);

      return returnedAction;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#REPLACE}. The replace action must not overlap
    * with an already existing remove or replace action. Otherwise this method will throw an
    * {@link InvalidOverlappingWriteException}. If the new {@link MediumAction} entirely contains one or several
    * existing {@link MediumAction} (i.e. is equal to it or fully encloses it), the existing {@link MediumAction} is
    * invalidated (i.e. its {@link MediumAction#isPending()} will return false) and removed from the
    * {@link MediumChangeManager}.
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
    *         zero-based sequence number that is guaranteed to be bigger than the sequence number of the last scheduled
    *         {@link MediumAction} (no matter what type) for the same {@link IMediumReference}, of 0 if there was no
    *         previous {@link MediumAction} scheduled at the {@link IMediumReference} of the given {@link MediumRegion}.
    */
   public MediumAction scheduleReplace(MediumRegion replacedRegion, ByteBuffer replacementBytes) {

      Reject.ifNull(replacedRegion, "replacedRegion");

      // Check if any INSERTs get in the way (case 5 in design decision DES 079 of design concept)
      // Note that getNextAction() will never return an INSERT at the same offset as the replace
      MediumAction nextAction = getNextAction(replacedRegion);

      if (nextAction != null && nextAction.getActionType() == MediumActionType.INSERT) {
         MediumRegion nextRegion = nextAction.getRegion();

         if (replacedRegion.contains(nextRegion.getStartReference())) {
            throw new InvalidOverlappingWriteException(nextAction, MediumActionType.INSERT, replacedRegion);
         }
      }

      // Check against existing removes and replaces
      Set<MediumActionType> typeSet = new HashSet<>();

      typeSet.add(MediumActionType.REMOVE);
      typeSet.add(MediumActionType.REPLACE);

      int nextSequenceNumber = handleExistingActionsOfTypes(typeSet, replacedRegion, MediumActionType.REPLACE);

      MediumAction returnedAction = new MediumAction(MediumActionType.REPLACE, replacedRegion, nextSequenceNumber,
         replacementBytes);
      mediumActions.add(returnedAction);

      return returnedAction;
   }

   /**
    * Schedules a new {@link MediumAction} of type {@link MediumActionType#INSERT}. The insert action may arbitrarily
    * overlap with any already existing actions.
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
    *         zero-based sequence number that is guaranteed to be bigger than the sequence number of the last scheduled
    *         {@link MediumAction} (no matter what type) for the same {@link IMediumReference}, of 0 if there was no
    *         previous {@link MediumAction} scheduled at the {@link IMediumReference} of the given {@link MediumRegion}.
    */
   public MediumAction scheduleInsert(MediumRegion insertionRegion, ByteBuffer insertionBytes) {

      Reject.ifNull(insertionRegion, "region");

      MediumAction previousAction = getPreviousAction(insertionRegion);

      // Check if any REPLACEs get in the way (case 7 in design decision DES 079 of design concept)
      // Note that getPreviousAction() will also return a REPLACE at the same offset as the insert

      if (previousAction != null && previousAction.getActionType() == MediumActionType.REPLACE) {
         MediumRegion previousRegion = previousAction.getRegion();

         if (previousRegion.contains(insertionRegion.getStartReference())
            && !insertionRegion.getStartReference().equals(previousRegion.getStartReference())) {
            throw new InvalidOverlappingWriteException(previousAction, MediumActionType.REPLACE, insertionRegion);
         }
      }

      int nextSequenceNumber = determineNextSequenceNumber(insertionRegion, previousAction);

      MediumAction returnedAction = new MediumAction(MediumActionType.INSERT, insertionRegion, nextSequenceNumber,
         insertionBytes);
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

   public List<MediumAction> createFlushPlan(int writeBlockSizeInBytes, long totalMediumSizeInBytes) {

      Reject.ifTrue(writeBlockSizeInBytes <= 0, "writeBlockSizeInBytes must be strictly positive");
      Reject.ifTrue(totalMediumSizeInBytes <= 0, "totalMediumSizeInBytes must be strictly positive");

      List<MediumAction> flushPlan = new ArrayList<>();
      List<ShiftedMediumBlock> mediumBlocks = new ArrayList<>();

      int delta = 0;

      Iterator<MediumAction> actionIterator = iterator();

      while (actionIterator.hasNext()) {
         MediumAction nextAction = actionIterator.next();
         MediumRegion nextRegion = nextAction.getRegion();

         ShiftedMediumBlock lastBlock = null;

         if (mediumBlocks.size() > 0) {
            lastBlock = mediumBlocks.get(mediumBlocks.size() - 1);
            if (delta == 0) {
               lastBlock.setEndReferenceOfMediumBytes(lastBlock.getStartReferenceOfFollowUpBytes());
            } else {
               if (lastBlock.getCausingAction().getSizeDelta() > 0
                  || !lastBlock.getCausingAction().getRegion().contains(nextRegion.getStartReference())) {
                  lastBlock.setEndReferenceOfMediumBytes(nextRegion.getStartReference());
               }
            }
         }

         delta += nextAction.getSizeDelta();

         if (lastBlock != null && lastBlock.getCausingAction().getSizeDelta() < 0
            && lastBlock.getCausingAction().getRegion().contains(nextRegion.getStartReference())) {
            int newBlockShift = (int) (delta + lastBlock.getCausingAction().getRegion().getSize()
               - nextRegion.getStartReference().getAbsoluteMediumOffset()
               + lastBlock.getCausingAction().getRegion().getStartReference().getAbsoluteMediumOffset());

            ShiftedMediumBlock newBlock = new ShiftedMediumBlock(nextAction, newBlockShift);
            newBlock.setEndReferenceOfMediumBytes(newBlock.getStartReferenceOfFollowUpBytes());
            mediumBlocks.add(mediumBlocks.size() - 1, newBlock);

            if (lastBlock.getCausingAction().getActionType() == MediumActionType.REPLACE) {
               lastBlock.setTotalShiftOfMediumBytes(delta - nextAction.getSizeDelta());
            } else {
               lastBlock.setTotalShiftOfMediumBytes(delta);
            }
         } else {
            mediumBlocks.add(new ShiftedMediumBlock(nextAction, delta));
         }
      }

      if (mediumBlocks.size() > 0) {
         ShiftedMediumBlock lastBlock = mediumBlocks.get(mediumBlocks.size() - 1);

         if (delta == 0) {
            lastBlock.setEndReferenceOfMediumBytes(lastBlock.getStartReferenceOfFollowUpBytes());
         } else {
            long startOfsLast = lastBlock.getStartReferenceOfFollowUpBytes().getAbsoluteMediumOffset();

            lastBlock.setEndReferenceOfMediumBytes(
               lastBlock.getStartReferenceOfFollowUpBytes().advance(totalMediumSizeInBytes - startOfsLast));
         }
      }

      mediumBlocks.sort(new ShiftedMediumBlockComparator());

      for (int i = 0; i < mediumBlocks.size(); ++i) {
         ShiftedMediumBlock element = mediumBlocks.get(i);

         flushPlan.addAll(element.computeResultingActions(writeBlockSizeInBytes));
      }

      // Add a truncate action to ensure the file is shortened, if necessary
      if (delta < 0) {
         IMediumReference truncateRef = mediumReferenceFactory.createMediumReference(totalMediumSizeInBytes + delta);

         flushPlan.add(new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(truncateRef, -delta), 0, null));
      }

      return flushPlan;
   }

   /**
    * Returns an {@link Iterator} of all {@link MediumAction}s currently maintained in this {@link MediumChangeManager}.
    * The order the {@link MediumAction}s are returned by this {@link Iterator} follows the order induced by the
    * {@link MediumActionComparator} class, i.e. sorted by {@link IMediumReference} and sequence number, ascending.
    * 
    * @return an {@link Iterator} of all {@link MediumAction}s currently maintained in this {@link MediumChangeManager}.
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
    * Gets the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}, according to the
    * {@link MediumActionComparator}, or null if there is none. According to the definition of
    * {@link MediumActionComparator}, the previous {@link MediumAction} with a smaller or equal {@link IMediumReference}
    * and smaller or equal sequence number will be returned by this method.
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
      // sequence number (due to the comparator implementation).
      MediumAction biggestPossibleAction = new MediumAction(MediumActionType.READ, newRegion, Integer.MAX_VALUE, null);

      return mediumActions.floor(biggestPossibleAction);
   }

   /**
    * Gets the next {@link MediumAction} already scheduled in this {@link MediumChangeManager}, according to the
    * {@link MediumActionComparator}, or null if there is none. According to the definition of
    * {@link MediumActionComparator}, the next {@link MediumAction} with a bigger or equal {@link IMediumReference} and
    * bigger or equal sequence number will be returned by this method. This method also returns any {@link MediumAction}
    * that has equal {@link IMediumReference} and equal sequence number, but differs in any other attribute.
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @return the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}
    */
   private MediumAction getNextAction(MediumRegion newRegion) {
      // NOTE: The MediumActionType does not matter for the biggest possible action, only the offset and the
      // sequence number (due to the comparator implementation).
      MediumAction biggestPossibleAction = new MediumAction(MediumActionType.READ, newRegion, Integer.MAX_VALUE, null);

      return mediumActions.ceiling(biggestPossibleAction);
   }

   /**
    * Checks and possibly invalidates any existing {@link MediumAction}s that were already scheduled against the
    * {@link MediumAction} to be newly created, identified by its {@link MediumActionType} and its {@link MediumRegion}.
    * Only a specific subset of existing {@link MediumAction}s is treated by this method, those having one of the given
    * types.
    * 
    * The cases that can occur:
    * <ul>
    * <li>Case 1: An existing region is fully contained in the new region, and thus the existing region will be undone
    * </li>
    * <li>Case 2: The new region is fully contained in an existing region, the new region is invalid and an
    * {@link InvalidOverlappingWriteException} is thrown</li>
    * <li>Case 3: An existing region is overlapped at its back by the new region, the new region is invalid and an
    * {@link InvalidOverlappingWriteException} is thrown</li>
    * <li>Case 4: An existing region is overlapped at its front by the new region, the new region is invalid and an
    * {@link InvalidOverlappingWriteException} is thrown</li>
    * </ul>
    * 
    * @param actionTypesToHandle
    *           Only existing {@link MediumAction}s of the {@link MediumActionType}s contained in this {@link Set} are
    *           checked or invalidated.
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @param newActionType
    *           The {@link MediumActionType} of the new {@link MediumAction} to be scheduled.
    * @return The next sequence number to take for the {@link MediumAction} to be scheduled.
    */
   private int handleExistingActionsOfTypes(Set<MediumActionType> actionTypesToHandle, MediumRegion newRegion,
      MediumActionType newActionType) {

      // NOTE: The previousAction may be one of the following:
      // - null, if there is no action that is smaller according to the MediumActionComparator OR
      // - the existing action with next smaller IMediumReference as the newRegion OR
      // - the existing action with next identical IMediumReference as the newRegion
      MediumAction previousAction = getPreviousAction(newRegion);

      while (previousAction != null && actionTypesToHandle.contains(previousAction.getActionType())) {

         MediumRegion previousRegion = previousAction.getRegion();

         MediumRegionOverlapType overlapType = MediumRegion.determineOverlapWithOtherRegion(newRegion, previousRegion);

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

      while (nextAction != null && actionTypesToHandle.contains(nextAction.getActionType())
         && !nextAction.equals(previousAction)) {

         MediumRegion nextRegion = nextAction.getRegion();

         MediumRegionOverlapType overlapType = MediumRegion.determineOverlapWithOtherRegion(newRegion, nextRegion);

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
            // The next region is not enclosed or overlapping, but simply has bigger start offset than end of new region
            break;
         }
      }

      return determineNextSequenceNumber(newRegion, previousAction);
   }

   /**
    * Determines the next sequence number to take for a new {@link MediumAction} to be scheduled based on the sequence
    * number of its previous {@link MediumAction}. Only if the previous {@link MediumAction} has the same
    * {@link IMediumReference}, its sequence number incremented by 1 is returned. Otherwise this method returns 0.
    * 
    * @param newRegion
    *           The new {@link MediumRegion} for which a new {@link MediumAction} is scheduled.
    * @param previousAction
    *           the previous {@link MediumAction} already scheduled in this {@link MediumChangeManager}
    * @return the next sequence number to take for a new {@link MediumAction} to be scheduled
    */
   private int determineNextSequenceNumber(MediumRegion newRegion, MediumAction previousAction) {
      int nextSequenceNumberAtOffset = 0;

      if (previousAction != null
         && previousAction.getRegion().getStartReference().equals(newRegion.getStartReference())) {

         if (previousAction.getSequenceNumber() == Integer.MAX_VALUE) {
            throw new IllegalStateException("Sequence numbers exhausted which should never happen");
         }

         nextSequenceNumberAtOffset = previousAction.getSequenceNumber() + 1;
      }

      return nextSequenceNumberAtOffset;
   }
}
