/**
 *
 * {@link JMetaRuntimeException}.java
 *
 * @author Jens Ebert
 *
 * @date 14.12.2017
 *
 */
package com.github.jmeta.utility.errors.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link JMetaRuntimeException} is the base class of all specific runtime exceptions of the library. It adds simple
 * logging support to each exception.
 */
public abstract class JMetaRuntimeException extends RuntimeException {

   private static final long serialVersionUID = 267998174522267962L;

   private static final Logger LOGGER = LoggerFactory.getLogger(JMetaRuntimeException.class);

   /**
    * Creates a new {@link JMetaRuntimeException}.
    * 
    * @param message
    *           The exception message or null
    * @param cause
    *           The causing exception or null
    */
   public JMetaRuntimeException(String message, Throwable cause) {
      this(message, cause, message);
   }

   /**
    * Creates a new {@link JMetaRuntimeException}.
    * 
    * @param message
    *           The exception message or null
    * @param cause
    *           The causing exception or null
    * @param logMessage
    *           A log message deviating from the exception message
    */
   public JMetaRuntimeException(String message, Throwable cause, String logMessage) {
      super(message, cause);

      if (LOGGER.isErrorEnabled()) {
         LOGGER.error(logMessage, this);
      }
   }
}
