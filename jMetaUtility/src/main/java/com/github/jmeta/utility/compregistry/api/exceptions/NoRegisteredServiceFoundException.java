package com.github.jmeta.utility.compregistry.api.exceptions;

import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * This exception is thrown whenever {@link ComponentRegistry#lookupService(Class)} is not able to find a service
 * provider implementation for a given service interface.
 */
public class NoRegisteredServiceFoundException extends JMetaRuntimeException {

   private static final long serialVersionUID = -7192364005474562382L;

   /**
    * @param message
    *           The exception message.
    */
   public NoRegisteredServiceFoundException(String message) {
      super(message, null);
   }
}
