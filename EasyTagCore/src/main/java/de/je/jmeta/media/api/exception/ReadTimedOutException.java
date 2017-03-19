/**
 *
 * {@link ReadTimedOutException}.java
 *
 * @author Jens Ebert
 *
 * @date 24.04.2011
 */
package de.je.jmeta.media.api.exception;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.IMediumStore;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link ReadTimedOutException} is thrown whenever a call to {@link IMediumStore#buffer(IMediumReference, long)} or
 * {@link IMediumStore#getData(IMediumReference, int)} has prematurely timed out.
 */
public class ReadTimedOutException extends MediumAccessException {

   private static final long serialVersionUID = 1L;

   private final int m_timeoutMillis;

   private final int m_bytesReallyRead;

   private final int m_byteCountTriedToRead;

   private final IMediumReference m_mediumReference;

   /**
    * Creates a new {@link ReadTimedOutException}.
    * 
    * @param timeoutMillis
    *           The timeout in milliseconds that caused the exception.
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
    * @pre timeoutMillis >= 0
    */
   public ReadTimedOutException(int timeoutMillis, int bytesRead,
      IMediumReference mediumReference, int byteCountTriedToRead) {

      super("Read timed out", null);

      Reject.ifNull(mediumReference, "mediumReference");
      Contract.checkPrecondition(bytesRead >= 0, "bytesRead >= 0");
      Contract.checkPrecondition(byteCountTriedToRead > 0,
         "byteCountTriedToRead > 0");
      Contract.checkPrecondition(timeoutMillis >= 0, "timeoutMillis >= 0");

      m_timeoutMillis = timeoutMillis;
      m_bytesReallyRead = bytesRead;
      m_byteCountTriedToRead = byteCountTriedToRead;
      m_mediumReference = mediumReference;
   }

   /**
    * Returns the number of bytes tried to read in the read attempt that caused the exception.
    * 
    * @return the number of bytes tried to read in the read attempt that caused the exception.
    */
   public int getByteCountTriedToRead() {

      return m_byteCountTriedToRead;
   }

   /**
    * Returns the number of bytes successfully read until the exception was thrown.
    * 
    * @return the number of bytes successfully read until the exception was thrown.
    */
   public int getBytesReallyRead() {

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
    * Returns the timeout in milliseconds that caused the exception.
    * 
    * @return the timeout in milliseconds that caused the exception.
    */
   public int getTimeoutValue() {

      return m_timeoutMillis;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[mediumReference=" + m_mediumReference
         + ", byteCountTriedToRead=" + m_byteCountTriedToRead
         + ", bytesReallyRead=" + m_bytesReallyRead + ", timeoutMillis="
         + m_timeoutMillis + "]";
   }
}
