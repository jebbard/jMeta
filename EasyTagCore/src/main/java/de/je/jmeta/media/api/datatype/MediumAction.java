/**
 *
 * {@link MediumAction}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */
package de.je.jmeta.media.api.datatype;

import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.OLD.IMediumStore_OLD;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link MediumAction} represents an action performed on a given {@link IMedium}, either a reading or a writing action.
 * A {@link MediumAction} belonging to an {@link IMediumStore_OLD} gets invalid as soon as the store is flushed or it is
 * undone. The validity of a {@link MediumAction} is represented by its {@link MediumAction#isPending} method. See the
 * methods description for more details.
 * 
 * Every {@link MediumAction} has a sequence number that denotes the order of execution of the {@link MediumAction} for
 * actions that have the same offset. Saying this, there could be several different {@link MediumAction}s referencing
 * the same offset, but that are executed one after the other.
 */
public class MediumAction {

   private final MediumActionType actionType;

   private final MediumRegion region;

   private final ByteBuffer actionBytes;

   private final int sequenceNumber;

   private boolean isPending;

   /**
    * Creates a new {@link MediumAction} in pending state.
    * 
    * @param actionType
    *           The {@link MediumActionType} of this {@link MediumAction}.
    * @param region
    *           The {@link MediumRegion} this {@link MediumAction} refers to. See {@link #getRegion()} for more details
    *           about the meanings of this region for every specific {@link MediumActionType}. Must never contain any
    *           {@link ByteBuffer} content, because it only determines the number of bytes affected.
    * @param sequenceNumber
    *           The sequence number of this {@link MediumAction}. Must be zero or positive.
    * @param actionBytes
    *           The bytes associated to this {@link MediumAction}. Depending in the type of the action, this can be
    *           replacement bytes, bytes to write or bytes to insert. For all other types, it must be null.
    */
   public MediumAction(MediumActionType actionType, MediumRegion region, int sequenceNumber, ByteBuffer actionBytes) {
      Reject.ifNull(region, "region");
      Reject.ifNull(actionType, "actionType");
      Reject.ifTrue(region.getBytes() != null,
         "The specified " + MediumRegion.class.getSimpleName()
            + " must not contain any bytes. The bytes associated with this " + MediumAction.class.getSimpleName()
            + " must be specified in the actionBytes parameter");
      Reject.ifTrue(sequenceNumber < 0, "sequenceNumber must be zero or positive");

      if (actionType == MediumActionType.INSERT || actionType == MediumActionType.REPLACE) {
         Reject.ifNull(actionBytes, "actionBytes");
      } else if (actionType != MediumActionType.WRITE) {
         Reject.ifTrue(actionBytes != null,
            "actionBytes must only be non-null for " + MediumActionType.class.getSimpleName() + "."
               + MediumActionType.INSERT + " and " + MediumActionType.class.getSimpleName() + "."
               + MediumActionType.REPLACE);
      }

      if (actionType == MediumActionType.INSERT) {
         Reject.ifTrue(region.getSize() != actionBytes.remaining(),
            "For type " + MediumActionType.class.getSimpleName() + "." + MediumActionType.INSERT
               + ", the size of the specified " + MediumRegion.class.getSimpleName()
               + " must match the remaining bytes of the given ByteBuffer");
      }

      this.sequenceNumber = sequenceNumber;
      this.actionBytes = actionBytes;
      this.actionType = actionType;
      this.isPending = true;
      this.region = region;
   }

   /**
    * Returns the {@link MediumActionType} of this {@link MediumAction}.
    * 
    * @return the {@link MediumActionType} of this {@link MediumAction}.
    */
   public MediumActionType getActionType() {

      return actionType;
   }

   /**
    * Returns the {@link MediumRegion} this {@link MediumAction} refers to. In any case, it contains the start offset of
    * the action to be performed on the external medium, and the number of affected bytes. That said, it must never
    * contain a {@link ByteBuffer} with actual bytes, because the bytes this action is associated with are stored in the
    * {@link ByteBuffer} returned by {@link #getActionBytes()}. The meaning of the size depending on the
    * {@link MediumActionType} is as follows:
    * <ul>
    * <li>For {@link MediumActionType#INSERT}: The number of bytes to be inserted at the given offset, i.e. must be
    * equal to the {@link ByteBuffer#remaining()} of the {@link ByteBuffer} returned by {@link #getActionBytes()}</li>
    * <li>For {@link MediumActionType#REPLACE}: The number of bytes to be replaced at the given offset, i.e. not
    * necessary equal to the {@link ByteBuffer#remaining()} of the {@link ByteBuffer} returned by
    * {@link #getActionBytes()}, because there can be more or less replacement bytes</li>
    * <li>For {@link MediumActionType#WRITE}: The number of bytes to be written at the given offset</li>
    * <li>For {@link MediumActionType#REMOVE}: The number of bytes to be removed at the given offset</li>
    * <li>For {@link MediumActionType#TRUNCATE}: The size has no specific meaning, as the number of bytes to be
    * truncated is already completely defined by the truncation offset. Truncations always affect the whole file
    * starting from the truncation offset.</li>
    * <li>For {@link MediumActionType#READ}: The number of bytes to be read at the given offset</li>
    * </ul>
    * 
    * @return region
    */
   public MediumRegion getRegion() {

      return region;
   }

   /**
    * Returns the bytes associated with this action defined as follows:
    * <ul>
    * <li>For {@link MediumActionType#INSERT}: The bytes to be inserted</li>
    * <li>For {@link MediumActionType#REPLACE}: The replacement bytes to be written</li>
    * <li>For {@link MediumActionType#WRITE}: null</li>
    * <li>For {@link MediumActionType#REMOVE}: null</li>
    * <li>For {@link MediumActionType#TRUNCATE}: null</li>
    * <li>For {@link MediumActionType#READ}: null</li>
    * </ul>
    * 
    * So even though {@link MediumActionType#WRITE} is a writing action, it has no bytes. The reason for this is that it
    * is only an <i>indication</i> of a write action that needs to be performed.
    * 
    * @return actionBytes the bytes associated with this action, or null if no action with associated bytes
    */
   public ByteBuffer getActionBytes() {
      return actionBytes;
   }

   /**
    * Returns the sequence number of this {@link MediumAction} (zero-based).
    * 
    * @return the insertion sequence number of this {@link MediumAction}.
    */
   public int getSequenceNumber() {

      return sequenceNumber;
   }

   /**
    * Sets this {@link MediumAction} to done, so it is no longer pending.
    */
   public void setDone() {

      this.isPending = false;
   }

   /**
    * Returns whether this {@link MediumAction} is pending, i.e. it still has to be applied successfully, or it is
    * already done. It returns true if this {@link MediumAction} is still valid and must still be flushed. It returns
    * false if this {@link MediumAction} has already been flushed or has been invalidated by undoing it.
    * 
    * @return true if this {@link MediumAction} is still pending, false if it is already applied.
    */
   public boolean isPending() {

      return isPending;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "MediumAction [actionType=" + actionType + ", region=" + region + ", actionBytes=" + actionBytes
         + ", sequenceNumber=" + sequenceNumber + ", isPending=" + isPending + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((actionBytes == null) ? 0 : actionBytes.hashCode());
      result = prime * result + ((actionType == null) ? 0 : actionType.hashCode());
      result = prime * result + sequenceNumber;
      result = prime * result + (isPending ? 1231 : 1237);
      result = prime * result + ((region == null) ? 0 : region.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MediumAction other = (MediumAction) obj;
      if (actionBytes == null) {
         if (other.actionBytes != null)
            return false;
      } else if (!actionBytes.equals(other.actionBytes))
         return false;
      if (actionType != other.actionType)
         return false;
      if (sequenceNumber != other.sequenceNumber)
         return false;
      if (isPending != other.isPending)
         return false;
      if (region == null) {
         if (other.region != null)
            return false;
      } else if (!region.equals(other.region))
         return false;
      return true;
   }

   /**
    * Returns the number of bytes by which the size of an external medium would increase or decrease if this
    * {@link MediumAction} would be applied to the medium. This method returns a positive integer for
    * {@link MediumAction}s of type {@link MediumActionType#INSERT}, as well as for {@link MediumAction}s of type
    * {@link MediumActionType#REPLACE}, if the number of replacement bytes is bigger than the number of replaced bytes.
    * It returns a negative integer for {@link MediumAction}s of types {@link MediumActionType#REMOVE} and
    * {@link MediumActionType#TRUNCATE}, which is the number of bytes removed or truncated, as well as for type
    * {@link MediumActionType#REPLACE}, if the number of replacement bytes is smaller than the number of replaced bytes.
    * It returns 0 for any other {@link MediumActionType} and for the special case of a {@link MediumActionType#REPLACE}
    * , if the number of replacement bytes equals the number of replaced bytes.
    * 
    * @return the number of bytes by which the size of an external medium would increase or decrease if this
    *         {@link MediumAction} would be applied to the medium.
    */
   public int getSizeDelta() {
      if (actionType == MediumActionType.INSERT) {
         return +getRegion().getSize();
      } else if (actionType == MediumActionType.REMOVE || actionType == MediumActionType.TRUNCATE) {
         return -getRegion().getSize();
      } else if (actionType == MediumActionType.REPLACE) {
         // NOTE: In case of an "inserting" replace, the number of replacement bytes is bigger than the number
         // of bytes to replace, i.e. a positive int will be returned. If it is in turn a "removing" replace,
         // a negative int will be returned.
         return getActionBytes().remaining() - getRegion().getSize();
      }

      return 0;
   }
}
