/**
 *
 * {@link UnsupportedMediumException}.java
 *
 * @author Jens Ebert
 *
 * @date 06.05.2011
 */
package de.je.jmeta.datablocks.impl;

/**
 * {@link UnsupportedMediumException}
 *
 */
public class UnsupportedMediumException extends RuntimeException {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link UnsupportedMediumException}.
    * 
    * @param message
    */
   public UnsupportedMediumException(String message) {
      super(message);
   }
}
