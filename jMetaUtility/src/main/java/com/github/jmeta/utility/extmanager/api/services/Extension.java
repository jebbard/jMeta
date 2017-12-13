/**
 *
 * {@link Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api.services;

import java.util.List;

import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link Extension} represents a jMeta extension that has an ID, an {@link ExtensionDescription} and provides service
 * implementations for a given number of service interfaces.
 *
 * Extensions should not do anything more than returning new instances of service implementations any time their methods
 * are called.
 */
public interface Extension {

   /**
    * Must return an ID of the extension that must be unique in jMeta. Preferably, you should use the package name of
    * the extension or the Maven group id connected to the artifact id. It is - however - also possible to return a
    * GUID. If the returned id is already used by another extension, this extension is rejected and not loaded.
    * 
    * @return an ID of the extension that must be unique in jMeta
    */
   public String getExtensionId();

   /**
    * @return an {@link ExtensionDescription} containing additional details about the extension
    */
   public ExtensionDescription getExtensionDescription();

   /**
    * Returns all implementation instances for the given service interface that are offered by this extension. It might
    * return an empty list if there are none. It must never return null.
    * 
    * @param serviceInterface
    *           The service interface queried
    * @param <S>
    *           The concrete service interface type
    * @return all implementation instances for the given service interface that are offered by this extension
    */
   public <S> List<S> getAllServiceProviders(Class<S> serviceInterface);
}
