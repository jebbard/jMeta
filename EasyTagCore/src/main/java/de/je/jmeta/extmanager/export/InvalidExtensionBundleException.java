package de.je.jmeta.extmanager.export;

/**
 * {@link InvalidExtensionBundleException} is thrown whenever a single {@link IExtensionBundle} could not be loaded due
 * to content or technical errors.
 */
public class InvalidExtensionBundleException extends Exception {

   /**
    * Creates a new {@link InvalidExtensionBundleException}.
    * 
    * @param message
    *           The message.
    * @param cause
    *           The causing exception, if any, or null.
    * @param bundleDesc
    *           the {@link ExtensionBundleDescription}. May be null if at the point of loading, the bundle description
    *           was not yet available.
    * @param reason
    *           The reason for this exception
    */
   public InvalidExtensionBundleException(String message, Throwable cause,
      ExtensionBundleDescription bundleDesc, ExtLoadExceptionReason reason) {
      super(message, cause);

      m_bundleDesc = bundleDesc;
      m_reason = reason;
   }

   /**
    * Returns the {@link ExtensionBundleDescription}. May be null if at the point of loading, the bundle description was
    * not yet available.
    *
    * @return the {@link ExtensionBundleDescription}. May be null if at the point of loading, the bundle description was
    *         not yet available.
    */
   public ExtensionBundleDescription getBundleDesc() {

      return m_bundleDesc;
   }

   /**
    * Returns the m_reason describing the cause of the exception.
    * 
    * @return The m_reason describing the cause of the exception.
    */
   public ExtLoadExceptionReason getReason() {

      return m_reason;
   }

   private final ExtensionBundleDescription m_bundleDesc;

   private final ExtLoadExceptionReason m_reason;

   private static final long serialVersionUID = 1L;
}
