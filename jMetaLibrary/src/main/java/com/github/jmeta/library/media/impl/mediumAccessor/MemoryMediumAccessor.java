/**
 * {@link MemoryMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents an in-memory, random-access {@link MediumAccessor}.
 */
public class MemoryMediumAccessor extends AbstractMediumAccessor<InMemoryMedium> {

   private byte[] memory;

   /**
    * Creates a new {@link MemoryMediumAccessor}.
    * 
    * @param medium
    *           The {@link AbstractMedium} this class works on
    */
   public MemoryMediumAccessor(InMemoryMedium medium) {
      super(medium);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor#isAtEndOfMedium()
    */
   @Override
   public boolean isAtEndOfMedium() {
      Reject.ifFalse(isOpened(), "isOpened()");

      return getCurrentPosition().getAbsoluteMediumOffset() >= getMedium().getCurrentLength();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificOpen()
    */
   @Override
   protected void mediumSpecificOpen() throws IOException {
      memory = getMedium().getWrappedMedium();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificClose()
    */
   @Override
   protected void mediumSpecificClose() {
      memory = null;
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(ByteBuffer)
    */
   @Override
   protected void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException {

      MediumOffset currentPosition = getCurrentPosition();
      final int currentOffset = (int) currentPosition.getAbsoluteMediumOffset();
      final int currentLength = (int) getMedium().getCurrentLength();

      int bytesToRead = buffer.remaining();
      int bytesReallyRead = bytesToRead;
      int initialPosition = buffer.position();

      // The end offset MUST point 1 byte behind the last byte to be read
      int readEndOffset = bytesToRead + currentOffset;

      final boolean readBeyondEOF = readEndOffset > currentLength;

      if (readBeyondEOF) {
         readEndOffset = currentLength;
         bytesReallyRead = readEndOffset - currentOffset;
      }

      buffer.put(ByteArrayUtils.copyOfRange(memory, currentOffset, readEndOffset));

      if (readBeyondEOF) {
         buffer.limit(initialPosition + bytesReallyRead);
         updateCurrentPosition(currentPosition.advance(bytesReallyRead));
         throw new EndOfMediumException(currentPosition, bytesToRead, bytesReallyRead, buffer);
      }

      updateCurrentPosition(currentPosition.advance(bytesReallyRead));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws IOException {

      final int numberOfBytesToWrite = buffer.remaining();

      final byte[] bytesToWrite = new byte[numberOfBytesToWrite];

      buffer.get(bytesToWrite);

      // Note: setCurrentPosition prevents offsets bigger than Integert.MAX_VALUE
      int absoluteMediumOffset = (int) getCurrentPosition().getAbsoluteMediumOffset();

      if (absoluteMediumOffset + bytesToWrite.length >= memory.length) {
         byte[] finalMediumBytes = new byte[absoluteMediumOffset + bytesToWrite.length];
         System.arraycopy(memory, 0, finalMediumBytes, 0, memory.length);
         memory = finalMediumBytes;
         getMedium().setBytes(finalMediumBytes);
      }

      System.arraycopy(bytesToWrite, 0, memory, absoluteMediumOffset, numberOfBytesToWrite);

      buffer.position(buffer.limit());

      updateCurrentPosition(getCurrentPosition().advance(numberOfBytesToWrite));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificTruncate()
    */
   @Override
   protected void mediumSpecificTruncate() {
      int newSize = (int) getCurrentPosition().getAbsoluteMediumOffset();

      byte[] newBytes = ByteArrayUtils.copyOfRange(memory, 0, newSize);

      memory = newBytes;
      getMedium().setBytes(newBytes);

   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(MediumOffset position) throws IOException {
      updateCurrentPosition(position);
   }
}
