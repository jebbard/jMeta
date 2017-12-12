/**
 * {@link InputStreamMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents a read-only streaming media {@link MediumAccessor} that may block when reading.
 */
public class InputStreamMediumAccessor extends AbstractMediumAccessor<InputStreamMedium> {

   private PushbackInputStream inputStream;

   /**
    * Creates a new {@link InputStreamMediumAccessor}.
    * 
    * @param medium
    *           The {@link InputStreamMedium} this class works on
    */
   public InputStreamMediumAccessor(InputStreamMedium medium) {
      super(medium);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public boolean isAtEndOfMedium() {
      Reject.ifFalse(isOpened(), "isOpened()");

      int size = 1;

      byte[] byteBuffer = new byte[size];

      int returnCode = 0;

      try {
         int bytesRead = 0;

         while (bytesRead < size) {
            returnCode = inputStream.read(byteBuffer, bytesRead, size - bytesRead);

            if (returnCode == -1) {
               throw new EndOfMediumException(getCurrentPosition(), size, bytesRead, ByteBuffer.allocate(0));
            }

            bytesRead += returnCode;
         }
      } catch (EndOfMediumException e) {
         return true;
      } catch (IOException e) {
         throw new MediumAccessException("IOException when trying to determine end of medium", e);
      }

      finally {
         try {
            // Only if previously a byte has been read, i.e. EOM was not reached: Unread it
            if (returnCode != -1) {
               inputStream.unread(byteBuffer[0]);
            }
         } catch (IOException e) {
            throw new MediumAccessException("IOException when calling PushbackInputStream.unread()", e);
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(MediumOffset,
    *      ByteBuffer)
    */
   @Override
   protected void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException {
      int bytesRead = 0;
      int size = buffer.remaining();
      int initialPosition = buffer.position();

      byte[] byteBuffer = new byte[size];

      MediumOffset currentPosition = getCurrentPosition();
      while (bytesRead < size) {
         int returnCode = inputStream.read(byteBuffer, bytesRead, size - bytesRead);

         if (returnCode == -1) {
            buffer.limit(initialPosition + bytesRead);
            updateCurrentPosition(currentPosition.advance(bytesRead));
            throw new EndOfMediumException(currentPosition, size, bytesRead, buffer);
         }

         bytesRead += returnCode;

         buffer.put(byteBuffer, buffer.position(), bytesRead);
      }

      updateCurrentPosition(currentPosition.advance(bytesRead));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(MediumOffset,
    *      ByteBuffer)
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(MediumOffset position) throws IOException {
      // Does nothing
   }
}
