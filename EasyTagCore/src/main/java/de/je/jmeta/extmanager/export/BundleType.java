/**
 *
 * {@link BundleType}.java
 *
 * @author Jens Ebert
 *
 * @date 06.05.2011
 */
package de.je.jmeta.extmanager.export;

/**
 * {@link BundleType} represents the type of extension bundle loaded. This can be used by client components to define a
 * behavior in case of multiple colliding extensions that may provide the same services.
 */
public enum BundleType {
   /**
    * The extension bundle is a default bundle that is included in the standard jMeta deployment version.
    */
   DEFAULT,
   /**
    * The extension bundle is a custom bundle provided by a 3rd party.
    */
   CUSTOM,
}
