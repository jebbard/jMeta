/**
 * {@link MemoryMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.github.jmeta.library.media.api.exception.EndOfMediumException;
import com.github.jmeta.library.media.api.type.AbstractMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.InMemoryMedium;

import de.je.util.javautil.common.array.EnhancedArrays;
import de.je.util.javautil.common.err.Reject;

/**
 * Represents an in-memory, random-access {@link IMediumAccessor}.
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.IMediumAccessor#isAtEndOfMedium(com.github.jmeta.library.media.api.type.IMediumReference)
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
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificRead(IMediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificRead(ByteBuffer buffer) throws IOException, EndOfMediumException {

      IMediumReference currentPosition = getCurrentPosition();
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

      buffer.put(EnhancedArrays.copyOfRange(memory, currentOffset, readEndOffset));

      if (readBeyondEOF) {
         buffer.limit(initialPosition + bytesReallyRead);
         updateCurrentPosition(currentPosition.advance(bytesReallyRead));
         throw new EndOfMediumException(bytesReallyRead, currentPosition, bytesToRead);
      }

      updateCurrentPosition(currentPosition.advance(bytesReallyRead));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws IOException {

      final int bytesToWrite = buffer.remaining();

      final byte[] bytes = (buffer.hasArray() ? buffer.array() : new byte[bytesToWrite]);

      // Note: setCurrentPosition prevents offsets bigger than Integert.MAX_VALUE
      int absoluteMediumOffset = (int) getCurrentPosition().getAbsoluteMediumOffset();

      if (absoluteMediumOffset + bytes.length >= memory.length) {
         byte[] finalMediumBytes = new byte[absoluteMediumOffset + bytes.length];
         System.arraycopy(memory, 0, finalMediumBytes, 0, memory.length);
         memory = finalMediumBytes;
         getMedium().setBytes(finalMediumBytes);
      }

      System.arraycopy(bytes, buffer.position(), memory, absoluteMediumOffset, bytesToWrite);

      buffer.position(buffer.limit());

      updateCurrentPosition(getCurrentPosition().advance(bytesToWrite));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificTruncate()
    */
   @Override
   protected void mediumSpecificTruncate() {
      int newSize = (int) getCurrentPosition().getAbsoluteMediumOffset();

      byte[] newBytes = EnhancedArrays.copyOfRange(memory, 0, newSize);

      memory = newBytes;
      getMedium().setBytes(newBytes);

   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessor#mediumSpecificSetCurrentPosition(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   protected void mediumSpecificSetCurrentPosition(IMediumReference position) throws IOException {
      Reject.ifTrue(position.getAbsoluteMediumOffset() > getMedium().getCurrentLength(),
         "position.getAbsoluteMediumOffset() > getMedium().getCurrentLength()");

      updateCurrentPosition(position);
   }
}
