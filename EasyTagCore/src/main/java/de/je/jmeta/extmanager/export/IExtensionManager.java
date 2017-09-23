/**
 * {@link IExtensionManager}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:20 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

import java.io.File;
import java.util.List;
import java.util.Set;

import de.je.util.javautil.io.stream.NamedInputStream;

/**
 * {@link IExtensionManager} is responsible for loading and returning all extensions that are available for jMeta. An
 * extension is represented by an interface that extends the {@link IExtensionPoint} interface, informally called
 * <i>extension point</i> and a single implementation of this interface called the <i>extension provider</i>.
 *
 * Extensions are bundled in so called extension bundles, represented by the interface {@link IExtensionBundle}. A
 * single extension bundle may contain an arbitrary number of extensions for any number of available extension points.
 */
public interface IExtensionManager {

   /**
    * Loads a new extension points configuration. Extension information determined from a previous file are discarded.
    * 
    * @param extensionPointStream
    *           The extension points configurations file. Must exist and be a file.
    * @param basePath
    *           Base path for all relative paths contained in the given configuration file. May be null to indicate that
    *           the current working directory is taken. If a path in the given configuration file is absolute, it is
    *           taken as is.
    * @return The exceptions occurred during loading the bundle.
    * @throws CouldNotLoadExtensionsException
    *            If a problem occurred during loading.
    *
    * @post {@link #isLoaded()}
    */
   public BundleLoadExceptions load(NamedInputStream extensionPointStream, File basePath);

   /**
    * Returns true if a extension points configuration file is currently loaded, false otherwise.
    *
    * @return true if a extension points configuration file is currently loaded, false otherwise.
    */
   public boolean isLoaded();

   /**
    * Returns all {@link IExtensionBundle}s registered with this {@link IExtensionManager}.
    *
    * @return all {@link IExtensionBundle}s registered with this {@link IExtensionManager}.
    */
   public List<IExtensionBundle> getRegisteredExtensionBundles();

   /**
    * Returns all available {@link IExtensionPoint} extending interfaces that can be implemented by a single extension.
    * These are the actual points where jMeta can be currently extended.
    *
    * @return all available {@link IExtensionPoint} extending interfaces that can be implemented by a single extension.
    *         These are the actual points where jMeta can be currently extended.
    */
   public Set<Class<? extends IExtensionPoint>> getAvailableExtensionPoints();

}
