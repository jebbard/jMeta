/**
 *
 * {@link InvalidArrayStringFormatException}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2011
 */
package com.github.jmeta.utility.byteutils.api.exceptions;

import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;

/**
 * {@link InvalidArrayStringFormatException} is thrown whenever the string array passed to
 * {@link ByteArrayUtils#parseArray(String)} has invalid format.
 */
public class InvalidArrayStringFormatException extends Exception {

   /**
    * Creates a new {@link InvalidArrayStringFormatException}.
    * 
    * @param message
    *           The message.
    */
   public InvalidArrayStringFormatException(String message) {
      super(message);
   }

   private static final long serialVersionUID = 1L;
}
