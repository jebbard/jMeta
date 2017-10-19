/**
 *
 * {@link UnknownCharsetException}.java
 *
 * @author Jens Ebert
 *
 * @date 01.06.2009
 *
 */
package de.je.util.javautil.common.charset;

import java.nio.charset.Charset;

/**
 * {@link UnknownCharsetException} is thrown whenever conversion of a {@link String} into a sequence of bytes or vice
 * versa using an unknown {@link Charset} is done.
 */
public class UnknownCharsetException extends RuntimeException {

   /**
    * Creates a new exception of this type.
    *
    * @param message
    *           The message to throw.
    * @param cause
    *           The causing {@link Throwable}.
    */
   public UnknownCharsetException(String message, Throwable cause) {
      super(message, cause);
   }

   private static final long serialVersionUID = 1L;
}
