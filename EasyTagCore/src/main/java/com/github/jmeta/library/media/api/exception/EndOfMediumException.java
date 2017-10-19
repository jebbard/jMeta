/**
 *
 * {@link EndOfMediumException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.12.2010
 */

package com.github.jmeta.library.media.api.exception;

import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link EndOfMediumException} is thrown whenever end of medium is reached while reading from an {@link IMedium}.
 */
public class EndOfMediumException extends Exception {

   private final int bytesReallyRead;

   private final int byteCountTriedToRead;

   private final IMediumReference mediumReference;

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link EndOfMediumException}.
    * 
    * @param bytesRead
    *           The number of bytes read before the exception was thrown.
    * @param mediumReference
    *           The start {@link IMediumReference} of the read attempt.
    * @param byteCountTriedToRead
    *           The number of bytes initially tried to read.
    * 
    * @pre bytesRead >= 0
    * @pre byteCountTriedToRead > 0
    */
   public EndOfMediumException(int bytesRead, IMediumReference mediumReference, int byteCountTriedToRead) {

      Reject.ifNegative(bytesRead, "bytesRead");
      Reject.ifNegativeOrZero(byteCountTriedToRead, "byteCountTriedToRead");

      this.bytesReallyRead = bytesRead;
      this.byteCountTriedToRead = byteCountTriedToRead;
      this.mediumReference = mediumReference;
   }

   /**
    * Returns the number of bytes tried to read in the read attempt that caused the exception.
    * 
    * @return the number of bytes tried to read in the read attempt that caused the exception.
    */
   public int getByteCountTriedToRead() {

      return byteCountTriedToRead;
   }

   /**
    * Returns the number of bytes successfully read until the exception was thrown.
    * 
    * @return the number of bytes successfully read until the exception was thrown.
    */
   public int getBytesReallyRead() {

      return bytesReallyRead;
   }

   /**
    * Returns the {@link IMediumReference} that was the starting point for the causing read attempt.
    * 
    * @return the {@link IMediumReference} that was the starting point for the causing read attempt.
    */
   public IMediumReference getMediumReference() {

      return mediumReference;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[m_mediumReference=" + mediumReference + ", m_byteCountTriedToRead="
         + byteCountTriedToRead + ", m_bytesReallyRead=" + bytesReallyRead + "]";
   }
}
