/**
 *
 * {@link JMetaIllegalStateException}.java
 *
 * @author Jens Ebert
 *
 * @date 14.12.2017
 *
 */
package com.github.jmeta.utility.errors.api.services;

/**
 * {@link JMetaIllegalStateException} is used to indicate unexpected states in complex operations. It is thus
 * semantically the same as a usual {@link IllegalStateException}, but with additional logging support.
 */
public class JMetaIllegalStateException extends JMetaRuntimeException {

   private static final long serialVersionUID = 3329067151944600693L;

   /**
    * Creates a new {@link JMetaIllegalStateException}.
    * 
    * @param message
    *           The exception message or null
    * @param cause
    *           The causing exception or null
    */
   public JMetaIllegalStateException(String message, Throwable cause) {
      super(message, cause, "Illegal state reached - application should be aborted:");
   }
}
