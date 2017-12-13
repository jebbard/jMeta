/**
 *
 * ShiftedMediumBlock.java
 *
 * @author Jens
 *
 * @date 25.07.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumRangeChunkAction;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ShiftedMediumBlock} represents a block of existing, consecutive {@link Medium} bytes which potentially need to
 * be shifted because of a causing {@link MediumAction}, e.g. an {@link MediumActionType#REMOVE} or
 * {@link MediumActionType#INSERT} operation. For random access media such as files or arrays, an insertion means that
 * all bytes behind the insertion index must be moved back by the number of inserted bytes. Likewise, for removals,
 * bytes behind the removed region must be moved forward to the index of removal. For replaces, it might be either a
 * move forward or backward, or even no move at all - depending on the number of bytes replaced and the number of
 * replacement bytes.
 * 
 * This class is a basic and important building block for the <i>create flush plan algorithm</i> (short hereafter:
 * <i>CFP algorithm</i>) that is implemented in {@link MediumChangeManager#createFlushPlan(int, long)}. The idea of the
 * CFP algorithm is that every insertion, removal or replacement leads to a "shift" of existing medium bytes.
 * 
 * All {@link MediumOffset} instances and sizes returned by this class refer to valid offsets and sizes on the existing,
 * external {@link Medium} and are therefore not in any way "virtual".
 * 
 * The actual size of the medium bytes is only computed during execution of the CFP algorithm. It is finally computable
 * after {@link #setTotalMediumByteCount(long)} has been called. In this state, the {@link ShiftedMediumBlock} is
 * finally assembled. You can see such an instance as referring to existing medium bytes including their location, and
 * at the same time telling <i>where</i> they will be moved after applying not only one action (i.e. the causing action
 * of this specific {@link ShiftedMediumBlock} instance), but a whole sequence of insertions, removals or replacements
 * on the external {@link Medium} until the start offset of this block.
 */
public class ShiftedMediumBlock {

   /**
    * Initial size of a {@link ShiftedMediumBlock}.
    */
   public static final int UNDEFINED_COUNT = -1;

   private final MediumAction causingAction;
   private final MediumOffset startReferenceOfFollowUpBytes;
   private final int totalShiftOfMediumBytes;
   private int totalMediumByteCount = UNDEFINED_COUNT;

   /**
    * Creates a new {@link ShiftedMediumBlock}.
    * 
    * @param causingAction
    *           The {@link MediumAction} that is the reason for creating this {@link ShiftedMediumBlock}.
    * @param totalShiftOfMediumBytes
    *           The number of bytes the existing medium bytes need to be shifted in total. This number might be 0,
    *           positive or negative. The number provided <i>already includes</i> the shift introduced by the causing
    *           action of this {@link ShiftedMediumBlock}, and therefore not only contains shifts by other actions at
    *           smaller offsets.
    */
   public ShiftedMediumBlock(MediumAction causingAction, int totalShiftOfMediumBytes) {

      Reject.ifNull(causingAction, "causingAction");

      this.causingAction = causingAction;
      this.totalShiftOfMediumBytes = totalShiftOfMediumBytes;
      this.startReferenceOfFollowUpBytes = initStartReference(getCausingAction());
   }

   /**
    * Returns the {@link MediumAction} that is the reason for creating this {@link ShiftedMediumBlock}.
    * 
    * @return the {@link MediumAction} that is the reason for creating this {@link ShiftedMediumBlock}.
    */
   public MediumAction getCausingAction() {
      return causingAction;
   }

   /**
    * Returns the start {@link MediumOffset} of the existing medium bytes this {@link ShiftedMediumBlock} refers to. It
    * must be clearly stated that these are the bytes <i>behind</i> the causing {@link MediumAction}s region, in detail:
    * <ul>
    * <li>For {@link MediumActionType#INSERT}: It is the start offset of the causing {@link MediumAction}, because bytes
    * are inserted before any existing medium bytes at the given offset</li>
    * <li>For {@link MediumActionType#REMOVE}: It is the offset of the first medium byte after the removed region</li>
    * <li>For {@link MediumActionType#REPLACE}: It is the offset of the first medium byte after the replaced region</li>
    * </ul>
    * 
    * @return the start {@link MediumOffset} of the existing medium bytes this {@link ShiftedMediumBlock} refers to.
    */
   public MediumOffset getStartReferenceOfFollowUpBytes() {
      return startReferenceOfFollowUpBytes;
   }

   /**
    * Sets the total number of consecutive medium bytes this {@link ShiftedMediumBlock} refers to. These are actual and
    * current bytes on the external medium.
    *
    * @param totalMediumByteCount
    *           the total number of existing medium bytes behind this {@link ShiftedMediumBlock}s action that are
    *           associated with it; might be strictly positive or zero. If it is bigger than {@link Integer#MAX_VALUE},
    *           an {@link IllegalStateException} is thrown, as we cannot handle distances between blocks that are bigger
    *           than this.
    */
   public void setTotalMediumByteCount(long totalMediumByteCount) {
      Reject.ifNegative(totalMediumByteCount, "totalMediumByteCount");

      if (totalMediumByteCount > Integer.MAX_VALUE) {
         throw new IllegalStateException(
            "Cannot handle changes whose distance is bigger than " + Integer.MAX_VALUE + " bytes, actual size: <"
               + totalMediumByteCount + ">. These changes must be executed within different flushes.");
      }

      this.totalMediumByteCount = (int) totalMediumByteCount;
   }

   /**
    * Returns a {@link MediumRegion} that represents the final location of the whole {@link ShiftedMediumBlock}, not
    * only including the original medium bytes behind the causing {@link MediumAction}, but also including the newly
    * written bytes, if any. Thus, the returned {@link MediumRegion} starts at the target offset of the {@link Medium}
    * where bytes will be written. It therefore reflects the state in which all {@link MediumAction}s are already
    * finally applied to the medium after a flush. Saying this, the offsets and sizes of the returned
    * {@link MediumRegion} usually do not yet refer to a valid region of the <i>current</i> medium content.
    * 
    * In detail, the region returned can be described as follows - in the listing "(+delta)" wants to express that all
    * offsets returned already include the total shift by any {@link MediumAction} changes at previous offsets:
    * <ul>
    * <li>For {@link MediumActionType#INSERT}: Starting at the insertion offset (+delta), ending at the byte before the
    * next block (or end of medium) (+delta), it encompasses the inserted bytes at the beginning and then the follow-up
    * medium bytes.</li>
    * <li>For {@link MediumActionType#REMOVE}: Starting at [remove offset + remove size] (+delta), ending at the byte
    * before the next block (or end of medium) (+delta), it of course does not encompass any previously removed bytes,
    * but only the follow-up medium bytes.</li>
    * <li>For {@link MediumActionType#REPLACE}: Starting at the replacement offset (+delta), ending at the byte before
    * the next block (or end of medium) (+delta), it encompasses the replacement bytes at the beginning and then the
    * follow-up medium bytes, sparing any previous medium bytes that might have been removed by this replace.</li>
    * </ul>
    * 
    * @return a {@link MediumRegion} that represents the final location of the whole block of {@link ShiftedMediumBlock}
    *         , not only including the original medium bytes behind the causing {@link MediumAction}, but also including
    *         the newly written bytes, if any.
    */
   public MediumRegion getTargetRegion() {
      MediumOffset changeOffset = getCausingAction().getRegion().getStartOffset();
      ByteBuffer actionBytes = getCausingAction().getActionBytes();

      int addedByteCount = 0;

      if (actionBytes != null) {
         addedByteCount = actionBytes.remaining();
      }

      // NOTE: We subtract the size delta of the causing action here because it is already included in the
      // totalShiftOfMediumBytes, and we only want to have the shift introduced by any previous offset changes here
      return new MediumRegion(changeOffset.advance(totalShiftOfMediumBytes - getCausingAction().getSizeDelta()),
         totalMediumByteCount + addedByteCount);
   }

   /**
    * Returns a {@link MediumRegion} that represents the source region of existing medium bytes that are shifted by this
    * {@link ShiftedMediumBlock}. The source region's medium bytes associated with this {@link ShiftedMediumBlock} are
    * those existing medium bytes behind the {@link MediumAction}'s last changed byte and before the next
    * {@link MediumAction} start offset or end of medium.
    * 
    * That said, any existing, but be removed or replaced bytes are not included, but the source region starts with the
    * first byte "surviving" behind the causing {@link MediumAction}. And - quite clear - the source region of course
    * does not contain any to be inserted or replacement bytes.
    * 
    * The source region is empty iff the {@link MediumAction} has {@link MediumActionType#INSERT} and there are schedule
    * actions at the same offset (if {@link MediumActionType#INSERT}: also with higher schedule sequence number). This
    * essentially has the meaning that this {@link MediumAction} has no associated bytes behind it, and the next
    * {@link MediumAction} (or the last one) at the same offset is associated with any existing follow-up bytes behind
    * the insertion offset.
    * 
    * @return a {@link MediumRegion} that represents the source region of existing medium bytes that are shifted by this
    *         {@link ShiftedMediumBlock}.
    */
   public MediumRegion getSourceRegion() {
      return new MediumRegion(getStartReferenceOfFollowUpBytes(), totalMediumByteCount);
   }

   /**
    * Computes and returns the {@link MediumAction}s resulting from this {@link ShiftedMediumBlock} based on a given
    * maximum write block size.
    * 
    * @param writeBlockSizeInBytes
    *           The maximum write block size used. As a result, no generated {@link MediumActionType#READ} or
    *           {@link MediumActionType#WRITE} action returned exceeds this maximum write block size.
    * @return The list of {@link MediumAction}s resulting from this {@link ShiftedMediumBlock}.
    */
   public List<MediumAction> computeResultingActions(int writeBlockSizeInBytes) {
      List<MediumAction> returnedActions = new ArrayList<>();

      int readWriteBlockCount = totalMediumByteCount / writeBlockSizeInBytes;
      int remainingByteCount = totalMediumByteCount % writeBlockSizeInBytes;

      MediumOffset startReadRef = null;

      boolean backwardReadWrite = totalShiftOfMediumBytes > 0;

      if (backwardReadWrite && readWriteBlockCount > 0) {
         startReadRef = getStartReferenceOfFollowUpBytes().advance(totalMediumByteCount);
      } else {
         startReadRef = getStartReferenceOfFollowUpBytes();
      }

      MediumOffset startWriteRef = startReadRef.advance(totalShiftOfMediumBytes);

      List<MediumAction> readWriteActions = new ArrayList<>();

      for (int i = 0; i < readWriteBlockCount; ++i) {
         if (backwardReadWrite) {
            startReadRef = startReadRef.advance(-writeBlockSizeInBytes);
            startWriteRef = startWriteRef.advance(-writeBlockSizeInBytes);
         }

         readWriteActions.add(
            new MediumAction(MediumActionType.READ, new MediumRegion(startReadRef, writeBlockSizeInBytes), 0, null));
         readWriteActions.add(
            new MediumAction(MediumActionType.WRITE, new MediumRegion(startWriteRef, writeBlockSizeInBytes), 0, null));

         if (!backwardReadWrite) {
            startReadRef = startReadRef.advance(+writeBlockSizeInBytes);
            startWriteRef = startWriteRef.advance(+writeBlockSizeInBytes);
         }

      }

      if (remainingByteCount > 0) {
         if (backwardReadWrite && readWriteBlockCount > 0) {
            startReadRef = startReadRef.advance(-remainingByteCount);
            startWriteRef = startWriteRef.advance(-remainingByteCount);
         }

         readWriteActions
            .add(new MediumAction(MediumActionType.READ, new MediumRegion(startReadRef, remainingByteCount), 0, null));
         readWriteActions.add(
            new MediumAction(MediumActionType.WRITE, new MediumRegion(startWriteRef, remainingByteCount), 0, null));
      }

      returnedActions.addAll(readWriteActions);

      if (causingAction.getActionType() == MediumActionType.INSERT
         || causingAction.getActionType() == MediumActionType.REPLACE) {

         int writtenByteCount = causingAction.getActionBytes().remaining();

         startWriteRef = causingAction.getRegion().getStartOffset()
            .advance(totalShiftOfMediumBytes - causingAction.getSizeDelta());

         final MediumOffset rangeStartRef = startWriteRef;

         byte[] insertBytes = causingAction.getActionBytes().array();

         List<MediumAction> writeActions = MediumRangeChunkAction.performActionOnChunksInRange(MediumAction.class,
            startWriteRef, writtenByteCount, writeBlockSizeInBytes,
            (MediumOffset chunkStartReference, int chunkSizeInBytes) -> {

               int startIndex = (int) chunkStartReference.distanceTo(rangeStartRef);

               return new MediumAction(MediumActionType.WRITE, new MediumRegion(chunkStartReference, chunkSizeInBytes),
                  0, ByteBuffer.wrap(insertBytes, startIndex, chunkSizeInBytes));
            });

         returnedActions.addAll(writeActions);
      }

      returnedActions.add(causingAction);

      return returnedActions;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      String sourceIntvString = "UNDEFINED";
      String targetIntvString = "UNDEFINED";

      if (totalMediumByteCount != UNDEFINED_COUNT) {
         MediumRegion sourceRegion = getSourceRegion();
         MediumRegion targetRegion = getTargetRegion();

         long sourceAbsoluteMediumOffset = sourceRegion.getStartOffset().getAbsoluteMediumOffset();

         sourceIntvString = sourceAbsoluteMediumOffset + ", " + sourceRegion.calculateEndOffsetAsLong();

         long targetAbsoluteMediumOffset = targetRegion.getStartOffset().getAbsoluteMediumOffset();

         targetIntvString = targetAbsoluteMediumOffset + ", " + targetRegion.calculateEndOffsetAsLong();
      }

      return "ShiftedMediumBlock [causingActionType=" + causingAction.getActionType() + ", causingActionRegOfs="
         + causingAction.getRegion().getStartOffset().getAbsoluteMediumOffset() + ", causingActionRegSize="
         + causingAction.getRegion().getSize() + ", srcRegnInterval=[" + sourceIntvString + "), targRegInterval=["
         + targetIntvString + "), totalShiftOfMediumBytes=" + totalShiftOfMediumBytes + "]";
   }

   /**
    * Initializes the start {@link MediumOffset} of the medium bytes as described in the javadocs of
    * {@link #getStartReferenceOfFollowUpBytes()}.
    * 
    * @param causingAction
    *           The causing {@link MediumAction} of this {@link ShiftedMediumBlock}.
    * @return The start {@link MediumOffset} of the medium bytes.
    */
   private static MediumOffset initStartReference(MediumAction causingAction) {
      MediumRegion region = causingAction.getRegion();

      if (causingAction.getActionType() == MediumActionType.INSERT) {
         // NOTE: The advance(0) is very important here to ensure that the start offset of the follow-up bytes is a
         // different (but equal) object! Reason: This reference might be used in cache to store follow-up-bytes read
         // behind the insert. Furthermore, if the flush finally happens, when shifting all created MediumOffsets by
         // this INSERT, all references behind or at equal offset, EXCEPT for the same object of this insert itself, are
         // shifted. Thus, if we would not advance(0) here, the same object would be used for the cache entry, and thus
         // it would wrongly not be shifted by the updateOffsets()
         return region.getStartOffset().advance(0);
      } else if (causingAction.getActionType() == MediumActionType.REMOVE
         || causingAction.getActionType() == MediumActionType.REPLACE) {
         return region.getStartOffset().advance(region.getSize());
      } else {
         throw new IllegalArgumentException("A causing action must have the type INSERT, REMOVE or REPLACE");
      }
   }

}
