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

/**
 * Represents an in-memory, random-access {@link IMediumAccessor}.
 */
public class MemoryMediumAccessor
   extends AbstractMediumAccessor<InMemoryMedium> {

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
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doClose()
    */
   @Override
   protected void doClose() throws Exception {

      memory = null;
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doOpen()
    */
   @Override
   protected void doOpen() throws Exception {

      memory = getMedium().getWrappedMedium();
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doRead(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doRead(IMediumReference reference, ByteBuffer buffer)
      throws IOException, EndOfMediumException {

      final int currentOffset = (int) reference.getAbsoluteMediumOffset();
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

      buffer.put(
         EnhancedArrays.copyOfRange(memory, currentOffset, readEndOffset));

      if (readBeyondEOF) {
         buffer.limit(initialPosition + bytesReallyRead);
         throw new EndOfMediumException(bytesReallyRead, reference,
            bytesToRead);
      }
   }

   /**
    * @see de.je.jmeta.media.impl.AbstractMediumAccessor#doWrite(IMediumReference, ByteBuffer)
    */
   @Override
   protected void doWrite(IMediumReference reference, ByteBuffer buffer)
      throws Exception {

      final int bytesToWrite = buffer.remaining();

      final byte[] bytes = (buffer.hasArray() ? buffer.array()
         : new byte[bytesToWrite]);

      System.arraycopy(bytes, buffer.position(), memory,
         (int) reference.getAbsoluteMediumOffset(), bytesToWrite);

      buffer.position(buffer.limit());
   }

   /**
    * @see de.je.jmeta.media.impl.IMediumAccessor#isAtEndOfMedium(de.je.jmeta.media.api.IMediumReference)
    */
   @Override
   public boolean isAtEndOfMedium(IMediumReference reference) {

      return reference.getAbsoluteMediumOffset() >= getMedium()
         .getCurrentLength();
   }
}
