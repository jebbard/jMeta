/**
 *
 * {@link ByteBufferUtils}.java
 *
 * @author Jens Ebert
 *
 * @date 05.03.2018
 *
 */
package com.github.jmeta.utility.byteutils.api.services;

import java.nio.ByteBuffer;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ByteBufferUtils} contains helper methods for working with {@link ByteBuffer}s.
 */
public class ByteBufferUtils {

   private static final byte[] EMPTY_ARRAY = {};

   /**
    * Copies the content of a {@link ByteBuffer} to a byte array, only taking all bytes between its current position and
    * its limit. The returned array is guaranteed to be only a copy of the {@link ByteBuffer}s original bytes.
    * 
    * @param buffer
    *           The {@link ByteBuffer} to copy
    * @return A copy of the {@link ByteBuffer}'s bytes between its current position and limit
    */
   public static byte[] asByteArrayCopy(ByteBuffer buffer) {
      Reject.ifNull(buffer, "buffer");

      return asByteArrayCopy(buffer, 0, buffer.remaining());
   }

   /**
    * Copies the content of a {@link ByteBuffer} to a byte array, only taking all bytes starting at its current position
    * plus "startIndex", and exactly "size" bytes. The returned array is guaranteed to be only a copy of the
    * {@link ByteBuffer}s original bytes.
    * 
    * @param buffer
    *           The {@link ByteBuffer} to copy
    * @param startIndex
    *           The zero-based start index of the first byte to copy, where 0 is equal to the {@link ByteBuffer}s
    *           current position; must not be equal to or bigger than the number of this buffer's remaining bytes
    * @param size
    *           The number of bytes to copy starting from position + startIndex; might be zero, but must not be
    *           negative; the buffer must have the same number or more remaining bytes at position + startIndex
    * @return A copy of "size" bytes from the {@link ByteBuffer} starting at its current position plus "startIndex"
    */
   public static byte[] asByteArrayCopy(ByteBuffer buffer, int startIndex, int size) {
      Reject.ifNull(buffer, "buffer");
      Reject.ifNegative(startIndex, "startIndex");
      Reject.ifNegative(size, "size");

      Reject.ifTrue(size > buffer.remaining() - startIndex, "size > buffer.remaining() - startIndex");

      if (size == 0) {
         return EMPTY_ARRAY;
      }

      byte[] byteArrayCopy = new byte[size];

      int currentIndex = buffer.position() + startIndex;

      for (int i = 0; i < size; i++, currentIndex++) {
         byteArrayCopy[i] = buffer.get(currentIndex);
      }

      return byteArrayCopy;
   }

   /**
    * Creates a new {@link ByteBufferUtils} instance.
    */
   private ByteBufferUtils() {
   }
}
