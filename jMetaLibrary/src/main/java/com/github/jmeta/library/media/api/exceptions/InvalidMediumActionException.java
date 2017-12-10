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

/**
 * {@link InvalidMediumActionException} is thrown whenever an unknown {@link MediumAction} is passed to
 * {@link MediumStore#undo(MediumAction)}.
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
