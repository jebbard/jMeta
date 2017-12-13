/**
 *
 * {@link EndOfMediumException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.12.2010
 */

package com.github.jmeta.library.media.api.exceptions;

import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link EndOfMediumException} is thrown whenever end of medium is reached while reading from an {@link Medium}.
 */
public class EndOfMediumException extends Exception {

   private final int byteCountActuallyRead;

   private final ByteBuffer bytesReadSoFar;

   private final int byteCountTriedToRead;

   private final MediumOffset readStartReference;

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link EndOfMediumException}.
    * 
    * @param readStartReference
    *           The start {@link MediumOffset} of the read attempt
    * @param byteCountTriedToRead
    *           The number of bytes initially tried to read
    * @param byteCountActuallyRead
    *           The number of bytes read before the exception was thrown
    * @param bytesReadSoFar
    *           The actual bytes read so far
    */
   public EndOfMediumException(MediumOffset readStartReference, int byteCountTriedToRead, int byteCountActuallyRead,
      ByteBuffer bytesReadSoFar) {

      Reject.ifNull(readStartReference, "mediumReference");
      Reject.ifNull(bytesReadSoFar, "bytesReadSoFar");
      Reject.ifNegative(byteCountActuallyRead, "byteCountActuallyRead");
      Reject.ifNegativeOrZero(byteCountTriedToRead, "byteCountTriedToRead");

      this.byteCountActuallyRead = byteCountActuallyRead;
      this.byteCountTriedToRead = byteCountTriedToRead;
      this.readStartReference = readStartReference;
      this.bytesReadSoFar = bytesReadSoFar;
   }

   /**
    * Returns the {@link MediumOffset} that was the starting point for the causing read attempt.
    * 
    * @return the {@link MediumOffset} that was the starting point for the causing read attempt.
    */
   public MediumOffset getReadStartReference() {

      return readStartReference;
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
   public int getByteCountActuallyRead() {

      return byteCountActuallyRead;
   }

   /**
    * Returns the bytes read by the last read attempt until the {@link EndOfMediumException} occurred.
    * 
    * @return the bytes read by the last read attempt until the {@link EndOfMediumException} occurred.
    */
   public ByteBuffer getBytesReadSoFar() {
      return bytesReadSoFar;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[readStartReference=" + readStartReference + ", byteCountTriedToRead="
         + byteCountTriedToRead + ", byteCountActuallyRead=" + byteCountActuallyRead + "]";
   }
}
