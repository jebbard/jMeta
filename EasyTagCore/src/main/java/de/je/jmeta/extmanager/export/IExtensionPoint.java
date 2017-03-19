/**
 * {@link IExtensionPoint}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:20 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

/**
 * An interface that extends {@link IExtensionPoint} can be referred to as a single place where jMeta can be extended.
 * The implementation of such an interface, also called extension provider, is the actual extension. There may be, of
 * course, multiple providers for a single extension point interface.
 *
 * Every extension point provider (i.e. implementor of this interface) should have a default constructor to ensure it
 * can be loaded by the runtime.
 */
public interface IExtensionPoint {

   /**
    * Returns the id of the extension provider which should be unique to be able to identify each single extension.
    *
    * @return the id of the extension provider which should be unique to be able to identify each single extension.
    */
   public String getExtensionId();
}
