package de.je.util.javautil.common.registry;

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
