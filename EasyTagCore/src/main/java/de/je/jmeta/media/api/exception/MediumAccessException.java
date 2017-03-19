/**
 * {@link MediumAccessException}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.IMedium;

/**
 * This exception is thrown whenever an {@link IMedium} could not be accessed.
 */
public class MediumAccessException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link MediumAccessException}.
    * 
    * @param message
    *           The message.
    * @param cause
    *           The cause.
    */
   public MediumAccessException(String message, Throwable cause) {

      super(message, cause);
   }
}
