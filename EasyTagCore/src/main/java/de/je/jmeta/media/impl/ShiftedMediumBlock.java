/**
 *
 * ShiftedMediumBlock.java
 *
 * @author Jens
 *
 * @date 25.07.2016
 *
 */
package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.MediumAction;
import de.je.jmeta.media.api.datatype.MediumActionType;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link ShiftedMediumBlock} represents a block of existing, consecutive {@link IMedium} bytes which potentially need
 * to be shifted because of a causing {@link MediumAction}, e.g. an {@link MediumActionType#REMOVE} or
 * {@link MediumActionType#INSERT} operation. For random access media such as files or arrays, an insertion means that
 * all bytes behind the insertion index must be moved back by the number of inserted bytes. Likewise, for removals,
 * bytes behind the removed region must be moved forward to the index of removal. For replaces, it might be either a
 * move forward or backward, or even no move at all - depending on the number of bytes replaced and the number of
 * replacement bytes.
 * 
 * This class is a basic and important building block for the <i>create flush plan algorithm</i> (short hereafter:
 * <i>CFP algorithm</i>) that is implemented in {@link MediumChangeManager#createFlushPlan(int, long)}. The idea of the
 * CFP algorithm is that every insertion, removal or replacement leads to a "shift" of existing medium bytes. This shift
 * is not only determined by the single "causing" {@link MediumAction} that is stored in this instance of
 * {@link ShiftedMediumBlock}, but also by other inserts, removes and replaces that occur before the causing action of
 * the current {@link ShiftedMediumBlock}. Thus, the actual number of bytes to shift (see
 * {@link #getTotalShiftOfMediumBytes()}) changes throughout the CFP algorithm, requiring a setter called
 * {@link #setTotalShiftOfMediumBytes(int)}. Likewise, the actual end offset of the byte block represented by an
 * instance of {@link ShiftedMediumBlock} is only determined during the algorithm, thus there is a setter for
 * {@link #setEndReferenceOfMediumBytes(IMediumReference)}.
 * 
 * All {@link IMediumReference} instances and sizes returned by this class refer to offsets and sizes on the external
 * {@link IMedium} and are therefore not in any way "virtual".
 * 
 * After instantiation, the {@link #getMediumByteCount()} method of this {@link ShiftedMediumBlock} returns
 * {@value #UNDEFINED_COUNT}. The reason for this is: The actual size of the medium bytes is only computed during
 * execution of the CFP algorithm. It is finally computable after
 * {@link #setEndReferenceOfMediumBytes(IMediumReference)} has been called. In this state, the
 * {@link ShiftedMediumBlock} is finally assembled. You can see such an instance as referring to existing medium bytes
 * including their location, and at the same time telling <i>where</i> they will be moved after applying not only one
 * action (i.e. the causing action of this specific {@link ShiftedMediumBlock} instance), but a whole sequence of
 * insertions, removals or replacements on the external {@link IMedium}.
 */
/**
 * {@link ShiftedMediumBlock}
 *
 */
public class ShiftedMediumBlock {

   /**
    * Initial size of a {@link ShiftedMediumBlock}.
    */
   public static final int UNDEFINED_COUNT = -1;

   private final MediumAction causingAction;
   private final IMediumReference startReferenceOfFollowUpBytes;
   private IMediumReference endReferenceOfFollowUpBytes;
   private int totalShiftOfMediumBytes;

   /**
    * Creates a new {@link ShiftedMediumBlock}.
    * 
    * @param causingAction
    *           The {@link MediumAction} that is the reason for creating this {@link ShiftedMediumBlock}.
    * @param totalShiftOfMediumBytes
    *           The number of bytes the existing medium bytes need to be shifted in total. This number might be 0,
    *           positive or negative. It changes during the create flush plan algorithm. Thus there is also a setter for
    *           this attribute. The number provided <i>already includes</i> the shift introduced by the causing action
    *           of this {@link ShiftedMediumBlock}, and therefore not only contains shifts by other actions at smaller
    *           offsets.
    */
   public ShiftedMediumBlock(MediumAction causingAction, int totalShiftOfMediumBytes) {

      Reject.ifNull(causingAction, "causingAction");

      this.causingAction = causingAction;
      this.totalShiftOfMediumBytes = totalShiftOfMediumBytes;
      this.startReferenceOfFollowUpBytes = initStartReference(getCausingAction());
   }

   /**
    * Initializes the start {@link IMediumReference} of the medium bytes as described in the javadocs of
    * {@link #getStartReferenceOfFollowUpBytes()}.
    * 
    * @param causingAction
    *           The causing {@link MediumAction} of this {@link ShiftedMediumBlock}.
    * @return The start {@link IMediumReference} of the medium bytes.
    */
   private static IMediumReference initStartReference(MediumAction causingAction) {
      MediumRegion region = causingAction.getRegion();

      if (causingAction.getActionType() == MediumActionType.INSERT) {
         return region.getStartReference();
      } else if (causingAction.getActionType() == MediumActionType.REMOVE
         || causingAction.getActionType() == MediumActionType.REPLACE) {
         return region.getStartReference().advance(region.getSize());
      } else {
         throw new IllegalArgumentException("A causing action must have the type INSERT, REMOVE or REPLACE");
      }
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
    * Returns the start {@link IMediumReference} of the existing medium bytes this {@link ShiftedMediumBlock} refers to.
    * It must be clearly stated that these are the bytes <i>behind</i> the causing {@link MediumAction}s region, in
    * detail:
    * <ul>
    * <li>For {@link MediumActionType#INSERT}: It is the start offset of the causing {@link MediumAction}, because bytes
    * are inserted before any existing medium bytes at the given offset</li>
    * <li>For {@link MediumActionType#REMOVE}: It is the offset of the first medium byte after the removed region</li>
    * <li>For {@link MediumActionType#REPLACE}: It is the offset of the first medium byte after the replaced region</li>
    * </ul>
    * 
    * @return the start {@link IMediumReference} of the existing medium bytes this {@link ShiftedMediumBlock} refers to.
    */
   public IMediumReference getStartReferenceOfFollowUpBytes() {
      return startReferenceOfFollowUpBytes;
   }

   /**
    * Returns the end {@link IMediumReference} of the medium bytes this {@link ShiftedMediumBlock} refers to. It is set
    * during execution of the CFP algorithm. Might return {@link #UNDEFINED_COUNT} if the CFP algorithm is still in
    * progress.
    * 
    * @return the end {@link IMediumReference} of the medium bytes this {@link ShiftedMediumBlock} refers to.
    */
   public IMediumReference getEndReferenceOfFollowUpBytes() {
      return endReferenceOfFollowUpBytes;
   }

   /**
    * Sets the end {@link IMediumReference} of the consecutive block of medium bytes this {@link ShiftedMediumBlock}
    * refers to. These are actual and current offsets on the external medium. Saying this, they not yet reflect any
    * shift operations by any already or not yet executed {@link MediumAction}s.
    * 
    * After this method has been called with a valid end {@link IMediumReference}, {@link #computeMediumByteCount()}
    * will return the corresponding size.
    *
    * @param endReferenceOfFollowUpBytes
    *           the end {@link IMediumReference} of the {@link ShiftedMediumBlock}.
    */
   public void setEndReferenceOfMediumBytes(IMediumReference endReferenceOfFollowUpBytes) {
      Reject.ifNull(endReferenceOfFollowUpBytes, "endReferenceOfFollowUpBytes");
      Reject.ifTrue(endReferenceOfFollowUpBytes.before(getStartReferenceOfFollowUpBytes()),
         "the given end reference <" + endReferenceOfFollowUpBytes + "> must be located before the start refernce <"
            + getStartReferenceOfFollowUpBytes() + ">.");

      this.endReferenceOfFollowUpBytes = endReferenceOfFollowUpBytes;
   }

   /**
    * Returns by how many bytes the existing medium bytes will be shifted by this {@link ShiftedMediumBlock} in total.
    * See the constructor javadocs for more details.
    * 
    * @return by how many bytes the existing medium bytes will be shifted by this {@link ShiftedMediumBlock} in total.
    *         See the constructor javadocs for more details.
    */
   public int getTotalShiftOfMediumBytes() {
      return totalShiftOfMediumBytes;
   }

   /**
    * Sets by how many bytes the existing medium bytes will be shifted by this {@link ShiftedMediumBlock} in total. See
    * the constructor javadocs for more details.
    *
    * @param totalShiftOfMediumBytes
    *           by how many bytes the existing medium bytes will be shifted by this {@link ShiftedMediumBlock} in total.
    *           See the constructor javadocs for more details.
    */
   public void setTotalShiftOfMediumBytes(int totalShiftOfMediumBytes) {
      Reject.ifNull(totalShiftOfMediumBytes, "totalShiftOfMediumBytes");

      this.totalShiftOfMediumBytes = totalShiftOfMediumBytes;
   }

   /**
    * Computes and returns the number of existing medium bytes between {@link #getStartReferenceOfFollowUpBytes()} and
    * {@link #getEndReferenceOfFollowUpBytes()} shifted by this {@link ShiftedMediumBlock}. It returns
    * {@link #UNDEFINED_COUNT} if the CFP algorithm is currently still in progress and the method
    * {@link #setEndReferenceOfMediumBytes(IMediumReference)} of this {@link ShiftedMediumBlock} has not yet been
    * called.
    * 
    * @return the number of medium bytes shifted by this {@link ShiftedMediumBlock}.
    */
   public int computeMediumByteCount() {

      if (endReferenceOfFollowUpBytes == null) {
         return UNDEFINED_COUNT;
      }

      return (int) getEndReferenceOfFollowUpBytes().distanceTo(getStartReferenceOfFollowUpBytes());
   }

   /**
    * Returns a {@link MediumRegion} that represents the final location of the whole {@link ShiftedMediumBlock}, not
    * only including the original medium bytes behind the causing {@link MediumAction}, but also including the newly
    * written bytes, if any. Thus, the returned {@link MediumRegion} starts at the target offset of the {@link IMedium}
    * where bytes will be written. It therefore reflects the state in which all {@link MediumAction}s are already
    * finally applied to the medium after a flush. Saying this, the offsets and sizes of the returned
    * {@link MediumRegion} usually do not yet refer to a valid region of the <i>current</i> medium content.
    * 
    * In detail, the region returned can be described as follows - in the listing "(+delta)" wants to express that all
    * offsets returned already include the total shift of the CFP algorithm:
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
      if (getCausingAction().getActionType() == MediumActionType.REMOVE) {
         return new MediumRegion(getStartReferenceOfFollowUpBytes().advance(getTotalShiftOfMediumBytes()),
            computeMediumByteCount());
      } else if (getCausingAction().getActionType() == MediumActionType.INSERT) {
         return new MediumRegion(getStartReferenceOfFollowUpBytes().advance(getTotalShiftOfMediumBytes()),
            computeMediumByteCount() + getCausingAction().getRegion().getSize());
      } else if (getCausingAction().getActionType() == MediumActionType.REPLACE) {
         int targetRegionStartRefAdvance = -getCausingAction().getRegion().getSize() + getTotalShiftOfMediumBytes()
            - (getCausingAction().getActionBytes().remaining() - getCausingAction().getRegion().getSize());

         IMediumReference targetRegionStartRef = getStartReferenceOfFollowUpBytes()
            .advance(targetRegionStartRefAdvance);

         return new MediumRegion(targetRegionStartRef,
            computeMediumByteCount() + getCausingAction().getActionBytes().remaining());
      } else {
         throw new IllegalStateException("Only actions of type INSERT, REPLACE and REMOVE are allowed");
      }
   }

   /**
    * Returns a {@link MediumRegion} that represents the source region of existing medium bytes that are shifted by this
    * {@link ShiftedMediumBlock}. However, the source region is not always the region between
    * {@link #getStartReferenceOfFollowUpBytes()} and {@link #getEndReferenceOfFollowUpBytes()}, but it is slightly more
    * complex:
    * <ul>
    * <li>For {@link MediumActionType#INSERT} and {@link MediumActionType#REMOVE}: It is the region between
    * {@link #getStartReferenceOfFollowUpBytes()} and {@link #getEndReferenceOfFollowUpBytes()}</li>
    * <li>For {@link MediumActionType#REPLACE}: The source region includes any to be replaced bytes. The reason is that
    * the CFP algorithm has to consider overlaps of these bytes, too</li>
    * </ul>
    * 
    * @return a {@link MediumRegion} that represents the source region of existing medium bytes that are shifted by this
    *         {@link ShiftedMediumBlock}.
    */
   public MediumRegion getSourceRegion() {
      if (getCausingAction().getActionType() == MediumActionType.REPLACE) {
         int replacedByteCount = getCausingAction().getRegion().getSize();

         return new MediumRegion(getStartReferenceOfFollowUpBytes().advance(-replacedByteCount),
            computeMediumByteCount() + replacedByteCount);
      }

      return new MediumRegion(getStartReferenceOfFollowUpBytes(), computeMediumByteCount());
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      String sourceIntvString = "UNDEFINED";
      String targetIntvString = "UNDEFINED";

      if ((this.endReferenceOfFollowUpBytes != null)) {
         MediumRegion sourceRegion = getSourceRegion();
         MediumRegion targetRegion = getTargetRegion();

         sourceIntvString = sourceRegion.getStartReference().getAbsoluteMediumOffset() + ", "
            + sourceRegion.calculateEndReference().getAbsoluteMediumOffset();

         targetIntvString = targetRegion.getStartReference().getAbsoluteMediumOffset() + ", "
            + targetRegion.calculateEndReference().getAbsoluteMediumOffset();
      }

      return "ShiftedMediumBlock [causingActionType=" + causingAction.getActionType() + ", causingActionRegOfs="
         + causingAction.getRegion().getStartReference().getAbsoluteMediumOffset() + ", causingActionRegSize="
         + causingAction.getRegion().getSize() + ", srcRegnInterval=[" + sourceIntvString + "), targRegInterval=["
         + targetIntvString + "), totalShiftOfMediumBytes=" + totalShiftOfMediumBytes + "]";
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

      int readWriteBlockCount = computeMediumByteCount() / writeBlockSizeInBytes;
      int remainingByteCount = computeMediumByteCount() % writeBlockSizeInBytes;

      IMediumReference startReadRef = null;

      boolean backwardReadWrite = totalShiftOfMediumBytes > 0;

      if (backwardReadWrite && readWriteBlockCount > 0) {
         startReadRef = getStartReferenceOfFollowUpBytes().advance(computeMediumByteCount());
      } else {
         startReadRef = getStartReferenceOfFollowUpBytes();
      }

      IMediumReference startWriteRef = startReadRef.advance(totalShiftOfMediumBytes);

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

         startWriteRef = causingAction.getRegion().getStartReference()
            .advance(totalShiftOfMediumBytes - causingAction.getSizeDelta());

         int writeBlockCount = writtenByteCount / writeBlockSizeInBytes;
         int remainingWriteByteCount = writtenByteCount % writeBlockSizeInBytes;

         List<MediumAction> writeActions = new ArrayList<>();

         byte[] insertBytes = causingAction.getActionBytes().array();

         for (int i = 0; i < writeBlockCount; ++i) {

            writeActions
               .add(new MediumAction(MediumActionType.WRITE, new MediumRegion(startWriteRef, writeBlockSizeInBytes), 0,
                  ByteBuffer.wrap(insertBytes, i * writeBlockSizeInBytes, writeBlockSizeInBytes)));

            startWriteRef = startWriteRef.advance(writeBlockSizeInBytes);
         }

         if (remainingWriteByteCount > 0) {
            writeActions
               .add(new MediumAction(MediumActionType.WRITE, new MediumRegion(startWriteRef, remainingWriteByteCount),
                  0, ByteBuffer.wrap(insertBytes, writeBlockCount * writeBlockSizeInBytes, remainingWriteByteCount)));
         }

         returnedActions.addAll(writeActions);
      }

      returnedActions.add(causingAction);

      return returnedActions;
   }

}
