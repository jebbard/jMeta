/**
 * {@link IExtensionBundle}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:20 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

import java.util.List;

/**
 * {@link IExtensionBundle} represents a bundle of several extensions. It has a name, a description and of course its
 * extensions in form of {@link IExtensionPoint} implementations.
 */
public interface IExtensionBundle {

   /**
    * Returns the informal name of the {@link IExtensionBundle} that can be used for identification purposes.
    *
    * @return the informal name of the {@link IExtensionBundle} that can be used for identification purposes
    */
   public String getName();

   /**
    * Returns the {@link ExtensionBundleDescription} of this {@link IExtensionBundle} that contains additional reference
    * information for the bundle.
    *
    * @return the {@link ExtensionBundleDescription} of this {@link IExtensionBundle} that contains additional reference
    *         information for the bundle.
    */
   public ExtensionBundleDescription getDescription();

   /**
    * Returns all extensions (in form of {@link IExtensionPoint} implementations) that are available in this
    * {@link IExtensionBundle} for the given specific extension point. The user refers to a specific extension point by
    * passing a {@link Class} instance of an interface the extends {@link IExtensionPoint}. The method then returns all
    * the extensions contained in this {@link IExtensionBundle} that implement the given {@link Class}.
    *
    * @param type
    *           The type of {@link IExtensionPoint} to get the extensions for.
    * @param <T>
    *           The concrete type of {@link IExtensionPoint} extending interface.
    *
    * @return All {@link IExtensionPoint} implementations available for the type of {@link IExtensionPoint} specified,
    *         may be an empty list.
    *
    * @throws UnknownExtensionPointException
    *            if the given type is not one of the available extension points.
    */
   public <T extends IExtensionPoint> List<T> getExtensionsForExtensionPoint(
      Class<T> type);

}
