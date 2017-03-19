/**
 * {@link UnknownExtensionPointException}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:18 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

/**
 * Is thrown whenever the client component requests the providers for an unsupported or unavailable
 * {@link IExtensionPoint} class.
 */
public class UnknownExtensionPointException extends RuntimeException {

   /**
    * Creates a new {@link UnknownExtensionPointException}.
    * 
    * @param message
    *           The message.
    */
   public UnknownExtensionPointException(String message) {
      super(message);
   }

   private static final long serialVersionUID = 1L;
}
