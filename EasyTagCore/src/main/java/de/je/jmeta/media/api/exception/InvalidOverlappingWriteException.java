/**
 *
 * InvalidOverlappingWriteException.java
 *
 * @author Jens
 *
 * @date 02.06.2016
 *
 */
package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.OLD.IMediumStore_OLD;
import de.je.jmeta.media.api.datatype.MediumAction;
import de.je.jmeta.media.api.datatype.MediumActionType;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link InvalidOverlappingWriteException} is thrown whenever one of the operations
 * {@link IMediumStore_OLD#replaceData(de.je.jmeta.media.api.IMediumReference, int, java.nio.ByteBuffer)} or
 * {@link IMediumStore_OLD#removeData(de.je.jmeta.media.api.IMediumReference, int)} is invoked before a
 * {@link IMediumStore_OLD#flush()}, and the operation overlaps with a previous remove or replace operation.
 */
public class InvalidOverlappingWriteException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private final MediumAction overlappedExistingAction;
   private final MediumRegion region;
   private final MediumActionType actionType;

   /**
    * Creates a new {@link InvalidOverlappingWriteException}.
    * 
    * @param overlappedExistingAction
    *           The existing {@link MediumAction} that was overlapped
    * @param actionType
    *           The {@link MediumActionType} of the write operation that lead to the exception
    * @param region
    *           The {@link MediumRegion} of the write operation that lead to the exception
    */
   public InvalidOverlappingWriteException(MediumAction overlappedExistingAction, MediumActionType actionType,
      MediumRegion region) {
      Reject.ifNull(overlappedExistingAction, "overlappedExistingAction");
      Reject.ifNull(actionType, "actionType");
      Reject.ifNull(region, "region");

      this.overlappedExistingAction = overlappedExistingAction;
      this.region = region;
      this.actionType = actionType;
   }

   /**
    * Returns the existing {@link MediumAction} that was overlapped
    * 
    * @return The existing {@link MediumAction} that was overlapped
    */
   public MediumAction getOverlappedExistingAction() {
      return overlappedExistingAction;
   }

   /**
    * Returns the {@link MediumRegion} of the write operation that lead to the exception
    * 
    * @return the {@link MediumRegion} of the write operation that lead to the exception
    */
   public MediumRegion getRegion() {
      return region;
   }

   /**
    * Returns the {@link MediumActionType} of the write operation that lead to the exception
    * 
    * @return the {@link MediumActionType} of the write operation that lead to the exception
    */
   public MediumActionType getActionType() {
      return actionType;
   }
}
