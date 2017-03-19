/**
 *
 * {@link ReadOnlyMediumException}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */
package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.IMedium;

/**
 * {@link ReadOnlyMediumException} is thrown whenever a read-only {@link IMedium} is accessed writing.
 */
public class ReadOnlyMediumException extends MediumAccessException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link ReadOnlyMediumException}.
    * 
    * @param medium
    *           The {@link IMedium} that is read-only
    * @param cause
    *           The cause
    */
   public ReadOnlyMediumException(IMedium<?> medium, Throwable cause) {
      super("Medium <" + medium + "> is read-only!", cause);
   }
}
