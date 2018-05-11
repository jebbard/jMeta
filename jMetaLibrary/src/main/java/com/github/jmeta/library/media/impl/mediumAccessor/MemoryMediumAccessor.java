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
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents an in-memory, random-access {@link MediumAccessor}.
 */
public class MemoryMediumAccessor extends AbstractMediumAccessor<InMemoryMedium> {

   private ByteBuffer memory;

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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(int)
    */
   @Override
   protected ByteBuffer mediumSpecificRead(int numberOfBytes) throws IOException, EndOfMediumException {
      ByteBuffer buffer = memory.asReadOnlyBuffer();

      try {
         MediumOffset currentPosition = getCurrentPosition();
         final int currentOffset = (int) currentPosition.getAbsoluteMediumOffset();
         final int currentLength = (int) getMedium().getCurrentLength();

         buffer.position(currentOffset);

         int bufferLimit = currentOffset + numberOfBytes;

         final boolean readBeyondEOF = bufferLimit > currentLength;

         if (readBeyondEOF) {
            buffer.mark();

            updateCurrentPosition(currentPosition.advance(currentLength - currentOffset));
            throw new EndOfMediumException(currentPosition, numberOfBytes, currentLength - currentOffset, buffer);
         }

         buffer.limit(numberOfBytes + currentOffset);

         buffer.mark();

         updateCurrentPosition(currentPosition.advance(numberOfBytes));

         return buffer;
      } finally {
         buffer.reset();
      }
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws IOException {
      final int numberOfBytesToWrite = buffer.remaining();

      // Note: setCurrentPosition prevents offsets bigger than Integert.MAX_VALUE
      int absoluteMediumOffset = (int) getCurrentPosition().getAbsoluteMediumOffset();

      ByteBuffer bufferToChange = null;

      if (absoluteMediumOffset + numberOfBytesToWrite >= memory.remaining()) {
         bufferToChange = ByteBuffer.allocate(absoluteMediumOffset + numberOfBytesToWrite);
         bufferToChange.put(memory);
      } else {
         bufferToChange = memory;
      }

      bufferToChange.position(absoluteMediumOffset);

      bufferToChange.put(buffer);
      bufferToChange.rewind();
      memory = bufferToChange;
      getMedium().setBytes(bufferToChange);

      updateCurrentPosition(getCurrentPosition().advance(numberOfBytesToWrite));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificTruncate()
    */
   @Override
   protected void mediumSpecificTruncate() {
      int newSize = (int) getCurrentPosition().getAbsoluteMediumOffset();
      ByteBuffer bufferToChange = ByteBuffer.allocate(newSize);
      memory.limit(newSize);
      bufferToChange.put(memory);
      bufferToChange.rewind();

      memory = bufferToChange;
      getMedium().setBytes(bufferToChange);
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(MediumOffset position) throws IOException {
      updateCurrentPosition(position);
   }
}
