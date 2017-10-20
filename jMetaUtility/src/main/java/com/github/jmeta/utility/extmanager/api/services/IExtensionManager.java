/**
 *
 * {@link IExtensionManager}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api.services;

import java.util.List;

/**
 * {@link IExtensionManager} provides access to all available {@link IExtension}s.
 */
public interface IExtensionManager {

   /**
    * @return all currently available {@link IExtension}s. None of the extensions returned will be null. If there are no
    *         extensions available, an empty list is returned.
    */
   public List<IExtension> getAllExtensions();
}
