/**
 *
 * {@link EndOfMediumException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.12.2010
 */

package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link EndOfMediumException} is thrown whenever end of medium is reached while reading from an {@link IMedium}.
 */
public class EndOfMediumException extends Exception {

   private final long m_bytesReallyRead;

   private final long m_byteCountTriedToRead;

   private final IMediumReference m_mediumReference;

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
   public EndOfMediumException(long bytesRead, IMediumReference mediumReference,
      long byteCountTriedToRead) {

      Reject.ifNull(mediumReference, "mediumReference");
      Contract.checkPrecondition(bytesRead >= 0, "bytesRead >= 0");
      Contract.checkPrecondition(byteCountTriedToRead > 0,
         "byteCountTriedToRead > 0");

      m_bytesReallyRead = bytesRead;
      m_byteCountTriedToRead = byteCountTriedToRead;
      m_mediumReference = mediumReference;
   }

   /**
    * Returns the number of bytes tried to read in the read attempt that caused the exception.
    * 
    * @return the number of bytes tried to read in the read attempt that caused the exception.
    */
   public long getByteCountTriedToRead() {

      return m_byteCountTriedToRead;
   }

   /**
    * Returns the number of bytes successfully read until the exception was thrown.
    * 
    * @return the number of bytes successfully read until the exception was thrown.
    */
   public long getBytesReallyRead() {

      return m_bytesReallyRead;
   }

   /**
    * Returns the {@link IMediumReference} that was the starting point for the causing read attempt.
    * 
    * @return the {@link IMediumReference} that was the starting point for the causing read attempt.
    */
   public IMediumReference getMediumReference() {

      return m_mediumReference;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[m_mediumReference=" + m_mediumReference
         + ", m_byteCountTriedToRead=" + m_byteCountTriedToRead
         + ", m_bytesReallyRead=" + m_bytesReallyRead + "]";
   }
}
