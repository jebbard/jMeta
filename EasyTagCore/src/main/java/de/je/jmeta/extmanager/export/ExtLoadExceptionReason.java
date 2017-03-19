/**
 *
 * {@link ExtLoadExceptionReason}.java
 *
 * @author Jens
 *
 * @date 23.05.2014
 *
 */
package de.je.jmeta.extmanager.export;

/**
 * {@link ExtLoadExceptionReason} defines reasons for failed extension loading.
 */
public enum ExtLoadExceptionReason {
   /**
    * Stream with extension configuration has invalid format.
    */
   STREAM_FORMAT_ERROR,

   /**
    * Stream with incompatible version information cannot be loaded.
    */
   STREAM_INCOMPATIBLE_VERSION,

   /**
    * An extension bundle path must be non-empty, existing and point to a directory.
    */
   INVALID_BUNDLE_PATH,

   /**
    * The interface for an extension point is missing, its class could not be loaded, is not an interface or it does not
    * extend from {@link IExtensionPoint}.
    */
   INVALID_EXTENSION_POINT_INTERFACE,

   /**
    * An extension point specified in an extension bundle is unknown
    */
   UNKNOWN_EXTENSION_POINT,

   /**
    * An extension provider specified in an extension bundle is invalid
    */
   INVALID_EXTENSION_PROVIDER,

   /**
    * An extension provider with invalid id
    */
   INVALID_EXTENSION_ID,

   /**
    * An relative path to an extension provider must be non-empty, existing and point to a file.
    */
   INVALID_PROVIDER_PATH,
}
