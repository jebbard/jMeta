/**
 *
 * {@link ReadOnlyMediumException}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */
package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.types.Medium;

/**
 * {@link ReadOnlyMediumException} is thrown whenever a read-only {@link Medium} is accessed writing.
 */
public class ReadOnlyMediumException extends MediumAccessException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link ReadOnlyMediumException}.
    * 
    * @param medium
    *           The {@link Medium} that is read-only
    * @param cause
    *           The cause
    */
   public ReadOnlyMediumException(Medium<?> medium, Throwable cause) {
      super("Medium <" + medium + "> is read-only!", cause);
   }
}
