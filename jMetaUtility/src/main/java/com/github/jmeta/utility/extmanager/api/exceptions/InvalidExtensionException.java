/**
 *
 * {@link InvalidExtensionException}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api.exceptions;

import com.github.jmeta.utility.extmanager.api.services.Extension;

/**
 * {@link InvalidExtensionException} is thrown whenever an {@link Extension} is determined to be invalid.
 */
public class InvalidExtensionException extends RuntimeException {

   private static final long serialVersionUID = 2901376365515630547L;

   /**
    * Creates a new {@link InvalidExtensionException}.
    * 
    * @param message
    *           the exception message
    * @param extension
    *           the extension
    */
   public InvalidExtensionException(String message, Extension extension) {
   }

}
