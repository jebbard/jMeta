package com.github.jmeta.utility.compregistry.api.exceptions;

import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;

/**
 * This exception is thrown whenever {@link ComponentRegistry#lookupService(Class)} is not able to find a service
 * provider implementation for a given service interface.
 */
public class NoRegisteredServiceFoundException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * @param message
    *           The exception message.
    */
   public NoRegisteredServiceFoundException(String message) {
      super(message);
   }
}
