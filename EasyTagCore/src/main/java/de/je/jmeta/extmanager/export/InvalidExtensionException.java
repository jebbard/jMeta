/**
 * {@link InvalidExtensionException}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:18 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link InvalidExtensionException} can be thrown by components using {@link IExtensionManager} to express that,
 * although an extension is technically correct and loaded successfully, it contains one or several semantic errors that
 * prohibit its further use.
 */
public class InvalidExtensionException extends RuntimeException {

   /**
    * Creates a new {@link InvalidExtensionException}.
    * 
    * @param message
    *           The message.
    * @param bundle
    *           The {@link IExtensionBundle} where the extension came from.
    * @param extension
    *           The {@link IExtensionPoint} implementation that represents the extension.
    */
   public InvalidExtensionException(String message, IExtensionBundle bundle,
      IExtensionPoint extension) {
      super(message);

      Reject.ifNull(extension, "extension");
      Reject.ifNull(bundle, "bundle");

      m_extension = extension;
      m_bundle = bundle;
   }

   /**
    * Returns the {@link IExtensionBundle}.
    *
    * @return the {@link IExtensionBundle}.
    */
   public IExtensionBundle getBundle() {

      return m_bundle;
   }

   /**
    * Returns the {@link IExtensionPoint}.
    *
    * @return the {@link IExtensionPoint}.
    */
   public IExtensionPoint getExtension() {

      return m_extension;
   }

   private static final long serialVersionUID = 1L;

   private final IExtensionBundle m_bundle;

   private final IExtensionPoint m_extension;
}
