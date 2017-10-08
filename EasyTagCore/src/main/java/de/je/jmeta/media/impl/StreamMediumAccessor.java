/**
 * {@link StreamMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.InputStreamMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.jmeta.media.api.exception.MediumAccessException;
import de.je.jmeta.media.api.exception.ReadTimedOutException;
import de.je.util.javautil.common.err.Reject;

/**
 * Represents a read-only streaming media {@link IMediumAccessor} that may block when reading.
 */
public class StreamMediumAccessor extends AbstractMediumAccessor<InputStreamMedium> {

   // /**
   // * The default timeout value used for this {@link StreamMediumAccessor} in milliseconds.
   // */
   // // TODO stage2_008: Reset this to 1000 (1 second) as soon as datablock test
   // // cases run again
   // public static final int DEFAULT_TIMEOUT_MILLIS = InputStreamMedium.NO_TIMEOUT;

   private static final ByteBuffer SINGLE_BYTE_BUFFER = ByteBuffer.allocate(1);

   private PushbackInputStream inputStream;

   /**
    * Creates a new {@link StreamMediumAccessor}.
    * 
    * @param medium
    *           The {@link InputStreamMedium} this class works on.
    */
   public StreamMediumAccessor(InputStreamMedium medium) {

      super(medium);
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#isAtEndOfMedium(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public boolean isAtEndOfMedium(IMediumReference reference) {
      Reject.ifFalse(isOpened(), "isOpened()");

      Reject.ifNull(reference, "reference");

      try {
         readWithoutTimeout(reference, SINGLE_BYTE_BUFFER);
      } catch (EndOfMediumException e) {
         assert e != null;
         return true;
      } catch (IOException e) {
         throw new MediumAccessException("IOException when trying to determine end of medium", e);
      }

      finally {
         try {
            // Only if previously a byte has been read: Unread it
            // Cases are:
            // - 1: Byte has been read without end of medium -> hasRemaining()
            // returns
            // false, limit equals capacity and position
            // - 2: End of medium occurred -> hasRemaining() returns false,
            // limit
            // equals position but does not equal capacity.
            if (SINGLE_BYTE_BUFFER.limit() == SINGLE_BYTE_BUFFER.capacity())
               inputStream.unread(SINGLE_BYTE_BUFFER.get(0));
         } catch (IOException e) {
            throw new MediumAccessException("IOException when calling PushbackInputStream.unread()", e);
         }

         finally {
            // Reset buffer to be able to read exactly one byte
            SINGLE_BYTE_BUFFER.clear();
         }
      }

      return false;
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doClose()
    */
   @Override
   protected void doClose() throws Exception {

      inputStream.close();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doOpen()
    */
   @Override
   protected void doOpen() throws Exception {

      inputStream = new PushbackInputStream(getMedium().getWrappedMedium());
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doRead(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doRead(IMediumReference reference, ByteBuffer buffer) throws IOException, EndOfMediumException {

      if (getMedium().getReadTimeout() == InputStreamMedium.NO_TIMEOUT)
         readWithoutTimeout(reference, buffer);

      else
         timedOutRead(reference, buffer);
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doWrite(IMediumReference reference, ByteBuffer buffer) throws Exception {

      // do nothing as this is a read-only class
   }

   /**
    * This version of read is called whenever a read is requested without a set timeout value. This version has the
    * possibility to hit end of medium, but it also may block forever wait for bytes to arrive.
    * 
    * @param reference
    *           The {@link StandardMediumReference} to start reading at.
    * @param buffer
    *           The {@link ByteBuffer} to store bytes read. The method tries to read up to buffer.remaining() bytes.
    * @throws IOException
    *            If an I/O operation failed.
    * @throws EndOfMediumException
    *            If end of medium was hit during reading.
    */
   private void readWithoutTimeout(IMediumReference reference, ByteBuffer buffer)
      throws IOException, EndOfMediumException {

      int bytesRead = 0;
      int size = buffer.remaining();
      int initialPosition = buffer.position();

      byte[] byteBuffer = new byte[size];

      while (bytesRead < size) {
         int returnCode = inputStream.read(byteBuffer, bytesRead, size - bytesRead);

         if (returnCode == -1) {
            buffer.limit(initialPosition + bytesRead);
            throw new EndOfMediumException(bytesRead, reference, size);
         }

         bytesRead += returnCode;

         buffer.put(byteBuffer, buffer.position(), bytesRead);
      }
   }

   /**
    * This version of read is called whenever a read is requested with a set timeout value. This version has the
    * possibility to throw a {@link ReadTimedOutException} (i.e. to time out prematurely before reading the full number
    * of bytes requested), but it may never hit end of medium.
    * 
    * @param reference
    *           The {@link StandardMediumReference} to start reading at.
    * @param buffer
    *           The {@link ByteBuffer} to store bytes read. The method tries to read up to buffer.remaining() bytes.
    * @throws IOException
    *            If an I/O operation failed.
    * @throws ReadTimedOutException
    *            If reading timed out.
    */
   private void timedOutRead(IMediumReference reference, ByteBuffer buffer) throws IOException {

      final int byteCount = buffer.remaining();
      final int readTimeout = getMedium().getReadTimeout();
      long sleepInterval = readTimeout > 1500 ? 250 : readTimeout / 4;
      int initialPosition = buffer.position();

      byte[] dataBuffer = new byte[byteCount];

      long currentTime = System.currentTimeMillis();
      long newTime = currentTime;

      int bytesRead = 0;

      while (newTime - currentTime < readTimeout && bytesRead < byteCount) {
         final int availableBytes = inputStream.available();

         if (availableBytes > 0) {
            int bytesLeftForReading = byteCount - bytesRead;
            int bytesToRead = availableBytes > bytesLeftForReading ? bytesLeftForReading : availableBytes;

            int currentBytesRead = inputStream.read(dataBuffer, bytesRead, bytesToRead);

            buffer.put(dataBuffer, bytesRead, currentBytesRead);

            bytesRead += currentBytesRead;
         }

         // Only sleep if still more bytes need to be read
         if (bytesRead < byteCount) {
            final long timeLeft = readTimeout - newTime + currentTime;

            if (timeLeft < sleepInterval)
               sleepInterval = timeLeft;

            try {
               Thread.sleep(sleepInterval);
            } catch (InterruptedException e) {
               // Reassert interrupt.
               Thread.currentThread().interrupt();
               // TODO media002: Is this an adequate handling of interruption
               // during sleeping?
               throw new RuntimeException("Unexpected thread interruption", e);
            }
         }

         newTime = System.currentTimeMillis();
      }

      // Loop terminated due to reached timeout
      if (bytesRead < byteCount) {
         buffer.limit(initialPosition + bytesRead);
         throw new ReadTimedOutException(readTimeout, bytesRead, reference, byteCount);
      }
   }
}
