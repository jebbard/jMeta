/**
 *
 * {@link InvalidExtensionException}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api.exception;

import com.github.jmeta.utility.extmanager.api.services.IExtension;

/**
 * {@link InvalidExtensionException}
 *
 */
public class InvalidExtensionException extends RuntimeException {

   /**
    * Creates a new {@link InvalidExtensionException}.
    * 
    * @param message
    * @param dataFormatsExtension
    */
   public InvalidExtensionException(String message, IExtension extension) {
   }

}
