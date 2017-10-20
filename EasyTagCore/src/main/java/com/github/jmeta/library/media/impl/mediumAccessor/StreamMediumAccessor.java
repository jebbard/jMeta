/**
 * {@link StreamMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exception.EndOfMediumException;
import com.github.jmeta.library.media.api.exception.MediumAccessException;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.InputStreamMedium;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents a read-only streaming media {@link IMediumAccessor} that may block when reading.
 */
public class StreamMediumAccessor extends AbstractMediumAccessor<InputStreamMedium> {

   private static final ByteBuffer SINGLE_BYTE_BUFFER = ByteBuffer.allocate(1);

   private PushbackInputStream inputStream;

   /**
    * Creates a new {@link StreamMediumAccessor}.
    * 
    * @param medium
    *           The {@link InputStreamMedium} this class works on
    */
   public StreamMediumAccessor(InputStreamMedium medium) {
      super(medium);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.IMediumAccessor#isAtEndOfMedium(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   public boolean isAtEndOfMedium() {
      Reject.ifFalse(isOpened(), "isOpened()");

      try {
         int bytesRead = 0;
         int size = SINGLE_BYTE_BUFFER.remaining();
         int initialPosition = SINGLE_BYTE_BUFFER.position();

         byte[] byteBuffer = new byte[size];

         while (bytesRead < size) {
            int returnCode = inputStream.read(byteBuffer, bytesRead, size - bytesRead);

            if (returnCode == -1) {
               SINGLE_BYTE_BUFFER.limit(initialPosition + bytesRead);
               updateCurrentPosition(getCurrentPosition().advance(bytesRead));
               throw new EndOfMediumException(bytesRead, getCurrentPosition(), size);
            }

            bytesRead += returnCode;

            SINGLE_BYTE_BUFFER.put(byteBuffer, SINGLE_BYTE_BUFFER.position(), bytesRead);
         }

         updateCurrentPosition(getCurrentPosition().advance(bytesRead));
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificOpen()
    */
   @Override
   protected void mediumSpecificOpen() throws IOException {
      inputStream = new PushbackInputStream(getMedium().getWrappedMedium());
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificClose()
    */
   @Override
   protected void mediumSpecificClose() throws IOException {
      inputStream.close();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(IMediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException {
      int bytesRead = 0;
      int size = buffer.remaining();
      int initialPosition = buffer.position();

      byte[] byteBuffer = new byte[size];

      IMediumReference currentPosition = getCurrentPosition();
      while (bytesRead < size) {
         int returnCode = inputStream.read(byteBuffer, bytesRead, size - bytesRead);

         if (returnCode == -1) {
            buffer.limit(initialPosition + bytesRead);
            updateCurrentPosition(currentPosition.advance(bytesRead));
            throw new EndOfMediumException(bytesRead, currentPosition, size);
         }

         bytesRead += returnCode;

         buffer.put(byteBuffer, buffer.position(), bytesRead);
      }

      updateCurrentPosition(currentPosition.advance(bytesRead));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws IOException {
      // do nothing as this is a read-only class
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificTruncate()
    */
   @Override
   protected void mediumSpecificTruncate() {
      // Not implemented
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(IMediumReference position) throws IOException {
      // Does nothing
   }
}
