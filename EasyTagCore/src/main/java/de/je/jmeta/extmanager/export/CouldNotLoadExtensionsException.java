/**
 *
 * {@link CouldNotLoadExtensionsException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.jmeta.extmanager.export;

/**
 * {@link CouldNotLoadExtensionsException} is thrown whenever the extension management component could not properly load
 * at startup. The client application should then terminate and analyze the log files for the problem source.
 */
public class CouldNotLoadExtensionsException extends RuntimeException {

   /**
    * Creates a new {@link CouldNotLoadExtensionsException}.
    * 
    * @param message
    *           The message.
    * @param cause
    *           The causing exception or null.
    * @param reason
    *           The m_reason describing the cause of the exception.
    */
   public CouldNotLoadExtensionsException(String message, Throwable cause,
      ExtLoadExceptionReason reason) {
      super(message, cause);

      m_reason = reason;
   }

   /**
    * Returns the reason describing the cause of the exception.
    * 
    * @return The reason describing the cause of the exception.
    */
   public ExtLoadExceptionReason getReason() {

      return m_reason;
   }

   private final ExtLoadExceptionReason m_reason;

   private static final long serialVersionUID = 1L;
}
