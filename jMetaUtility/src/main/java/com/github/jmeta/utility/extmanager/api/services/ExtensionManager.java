/**
 *
 * {@link ExtensionManager}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api.services;

import java.util.List;

/**
 * {@link ExtensionManager} provides access to all available {@link Extension}s.
 */
public interface ExtensionManager {

   /**
    * @return all currently available {@link Extension}s. None of the extensions returned will be null. If there are no
    *         extensions available, an empty list is returned.
    */
   public List<Extension> getAllExtensions();
}
