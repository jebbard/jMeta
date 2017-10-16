/**
 *
 * {@link StandardExtensionManager}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package de.je.jmeta.extmanager.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import de.je.jmeta.extmanager.api.IExtension;
import de.je.jmeta.extmanager.api.IExtensionManager;

/**
 * {@link StandardExtensionManager} is the default implementation of {@link IExtensionManager}.
 */
public class StandardExtensionManager implements IExtensionManager {

   private final List<IExtension> allExtensionsFound = new ArrayList<>();

   /**
    * Creates a new {@link StandardExtensionManager}. Caches all {@link IExtension}s to be returned by
    * {@link #getAllExtensions()}.
    */
   public StandardExtensionManager() {
      Iterator<IExtension> extensions = ServiceLoader.load(IExtension.class).iterator();

      extensions.forEachRemaining((ext) -> allExtensionsFound.add(ext));
   }

   /**
    * @see de.je.jmeta.extmanager.api.IExtensionManager#getAllExtensions()
    */
   @Override
   public List<IExtension> getAllExtensions() {
      return allExtensionsFound;
   }

}
