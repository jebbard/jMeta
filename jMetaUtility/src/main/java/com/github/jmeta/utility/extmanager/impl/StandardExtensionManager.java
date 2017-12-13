/**
 *
 * {@link StandardExtensionManager}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.services.ExtensionManager;

/**
 * {@link StandardExtensionManager} is the default implementation of {@link ExtensionManager}.
 */
public class StandardExtensionManager implements ExtensionManager {

   private final List<Extension> allExtensionsFound = new ArrayList<>();

   /**
    * Creates a new {@link StandardExtensionManager}. Caches all {@link Extension}s to be returned by
    * {@link #getAllExtensions()}.
    */
   public StandardExtensionManager() {
      Iterator<Extension> extensions = ServiceLoader.load(Extension.class).iterator();

      extensions.forEachRemaining((ext) -> allExtensionsFound.add(ext));
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.ExtensionManager#getAllExtensions()
    */
   @Override
   public List<Extension> getAllExtensions() {
      return allExtensionsFound;
   }

}
