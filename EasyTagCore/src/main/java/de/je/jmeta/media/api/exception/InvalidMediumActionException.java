/**
 *
 * InvalidMediumActionException.java
 *
 * @author Jens
 *
 * @date 03.06.2016
 *
 */
package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.IMediumStore;
import de.je.jmeta.media.api.datatype.MediumAction;

/**
 * {@link InvalidMediumActionException} is thrown whenever an unknown {@link MediumAction} is passed to
 * {@link IMediumStore#undo(MediumAction)}.
 */
public class InvalidMediumActionException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   private final MediumAction invalidAction;

   /**
    * Creates a new {@link InvalidMediumActionException}.
    * 
    * @param invalidAction
    *           the invalid {@link MediumAction} that lead to the exception.
    */
   public InvalidMediumActionException(MediumAction invalidAction) {
      this.invalidAction = invalidAction;
   }

   /**
    * Returns the invalid {@link MediumAction} that lead to the exception.
    * 
    * @return the invalid {@link MediumAction} that lead to the exception.
    */
   public MediumAction getInvalidAction() {
      return invalidAction;
   }
}
