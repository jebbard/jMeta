/**
 *
 * {@link BundleLoadExceptions}.java
 *
 * @author Jens
 *
 * @date 25.05.2014
 *
 */
package de.je.jmeta.extmanager.export;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * {@link BundleLoadExceptions} stores any {@link InvalidExtensionBundleException}s that occured during loading of the
 * extension bundles.
 */
public class BundleLoadExceptions {

   /**
    * Adds a new inner {@link Exception} to this enclosing exception.
    * 
    * @param configFile
    *           The config {@link File} of the faulty extension bundle.
    * @param e
    *           The {@link Exception} that occurred when trying to load the bundle.
    */
   public void addExtensionLoadingException(File configFile,
      InvalidExtensionBundleException e) {

      exceptions.put(configFile, e);
   }

   /**
    * Returns the internal exceptions leading to this {@link InvalidExtensionBundleException} for each affected
    * extension bundle.
    * 
    * @return the internal exceptions leading to this {@link InvalidExtensionBundleException}.
    */
   public Map<File, InvalidExtensionBundleException> getExceptions() {

      return Collections.unmodifiableMap(exceptions);
   }

   private Map<File, InvalidExtensionBundleException> exceptions = new HashMap<>();
}
