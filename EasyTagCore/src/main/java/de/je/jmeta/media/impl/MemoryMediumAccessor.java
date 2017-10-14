/**
 * {@link MemoryMediumAccessor}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package de.je.jmeta.media.impl;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.util.javautil.common.array.EnhancedArrays;
import de.je.util.javautil.common.err.Reject;

/**
 * Represents an in-memory, random-access {@link IMediumAccessor}.
 */
public class MemoryMediumAccessor extends AbstractMediumAccessor<InMemoryMedium> {

   @Override
   protected void mediumSpecificTruncate() {
      int newSize = (int) getCurrentPosition().getAbsoluteMediumOffset();

      byte[] newBytes = EnhancedArrays.copyOfRange(memory, 0, newSize);

      memory = newBytes;
      getMedium().setBytes(newBytes);

   }

   private byte[] memory;

   /**
    * Creates a new {@link MemoryMediumAccessor}.
    * 
    * @param medium
    *           The {@link AbstractMedium} this class works on.
    */
   public MemoryMediumAccessor(InMemoryMedium medium) {

      super(medium);
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#mediumSpecificClose()
    */
   @Override
   protected void mediumSpecificClose() throws Exception {

      memory = null;
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#mediumSpecificOpen()
    */
   @Override
   protected void mediumSpecificOpen() throws Exception {

      memory = getMedium().getWrappedMedium();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#mediumSpecificRead(IMediumReference, ByteBuffer)
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
         setCurrentPositionInternal(currentPosition.advance(bytesReallyRead));
         throw new EndOfMediumException(bytesReallyRead, currentPosition, bytesToRead);
      }

      setCurrentPositionInternal(currentPosition.advance(bytesReallyRead));
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#mediumSpecificWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void mediumSpecificWrite(ByteBuffer buffer) throws Exception {

      final int bytesToWrite = buffer.remaining();

      final byte[] bytes = (buffer.hasArray() ? buffer.array() : new byte[bytesToWrite]);

      // TODO handle case of too big offset!

      int absoluteMediumOffset = (int) getCurrentPosition().getAbsoluteMediumOffset();

      if (absoluteMediumOffset + bytes.length >= memory.length) {
         byte[] finalMediumBytes = new byte[absoluteMediumOffset + bytes.length];
         System.arraycopy(memory, 0, finalMediumBytes, 0, memory.length);
         memory = finalMediumBytes;
         getMedium().setBytes(finalMediumBytes);
      }

      System.arraycopy(bytes, buffer.position(), memory, absoluteMediumOffset, bytesToWrite);

      buffer.position(buffer.limit());

      setCurrentPositionInternal(getCurrentPosition().advance(bytesToWrite));
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#isAtEndOfMedium(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public boolean isAtEndOfMedium() {
      Reject.ifFalse(isOpened(), "isOpened()");

      return getCurrentPosition().getAbsoluteMediumOffset() >= getMedium().getCurrentLength();
   }
}
