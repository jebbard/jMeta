/**
 *
 * InvalidMediumActionException.java
 *
 * @author Jens
 *
 * @date 03.06.2016
 *
 */
package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * {@link InvalidMediumActionException} is thrown whenever an unknown {@link MediumAction} is passed to
 * {@link MediumStore#undo(MediumAction)}.
 */
public class InvalidMediumActionException extends JMetaRuntimeException {

   private static final long serialVersionUID = -7554544649476371997L;

   private final MediumAction invalidAction;

   /**
    * Creates a new {@link InvalidMediumActionException}.
    * 
    * @param invalidAction
    *           the invalid {@link MediumAction} that lead to the exception.
    */
   public InvalidMediumActionException(MediumAction invalidAction) {
      super("Invalid medium action: " + invalidAction, null);
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
