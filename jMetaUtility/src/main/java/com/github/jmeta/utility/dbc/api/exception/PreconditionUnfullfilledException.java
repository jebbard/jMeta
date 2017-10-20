/**
 *
 * {@link PreconditionUnfullfilledException}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2008
 *
 */

package com.github.jmeta.utility.dbc.api.exception;

/**
 * {@link PreconditionUnfullfilledException} is thrown whenever a specified prerequisite-condition of a class is violated.
 */
public class PreconditionUnfullfilledException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link PreconditionUnfullfilledException}.
    * 
    * @param message
    *           The message containing the cause of this exception.
    */
   public PreconditionUnfullfilledException(String message) {
      super(message);
   }

   /**
    * Creates a new {@link PreconditionUnfullfilledException}.
    */
   public PreconditionUnfullfilledException() {
   }
}
