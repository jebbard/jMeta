/**
 *
 * {@link FieldTerminationFinder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.02.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinder}
 *
 */
public class FieldTerminationFinder {

   @FunctionalInterface
   interface FieldDataProvider {

      ByteBuffer nextData(int byteCount);
   }

   public final static long NO_LIMIT = -1;

   public long getSizeUntilTermination(Charset charset, Character terminationCharacter, FieldDataProvider dataProvider,
      long limit, int readBlockSize) {

      long sizeUpToEndOfTerminationBytes = 0;

      // If -1, this variable indicates no termination bytes have been found yet, if > 0 it is their current loop's
      // index
      int lenUpToTermination = -1;

      ByteBuffer encodedBytes = ByteBuffer.allocate(0);

      CharsetDecoder decoder = charset.newDecoder();
      decoder.onMalformedInput(CodingErrorAction.REPLACE);
      decoder.onUnmappableCharacter(CodingErrorAction.REPLACE);

      do {
         encodedBytes = getNextEncodedBytes(encodedBytes, dataProvider, readBlockSize);

         if (!encodedBytes.hasRemaining()) {
            return sizeUpToEndOfTerminationBytes;
         }

         CharBuffer outputBuffer = CharBuffer.allocate(encodedBytes.remaining());
         CoderResult result = decoder.decode(encodedBytes, outputBuffer, false);

         // This should never happen as allocate as much characters as there are bytes
         if (result.isOverflow()) {
            throw new IllegalStateException(
               "Output buffer overflow when decoding a byte sequence to charset " + charset);
         }

         outputBuffer.flip();

         String bufferString = outputBuffer.toString();

         lenUpToTermination = bufferString.indexOf(terminationCharacter);

         boolean isFollowUpBlock = sizeUpToEndOfTerminationBytes > 0;

         sizeUpToEndOfTerminationBytes += getDeltaSizeUpToEndOfTerminationBytes(charset, lenUpToTermination,
            bufferString, isFollowUpBlock);

         if (limit != NO_LIMIT && sizeUpToEndOfTerminationBytes >= limit) {
            return limit;
         }
      } while (lenUpToTermination == -1);

      return sizeUpToEndOfTerminationBytes;
   }

   private ByteBuffer getNextEncodedBytes(ByteBuffer previousEncodedBytes, FieldDataProvider dataProvider,
      int readBlockSize) {
      ByteBuffer tempBuffer = ByteBuffer.allocate(readBlockSize + previousEncodedBytes.remaining());
      tempBuffer.put(previousEncodedBytes);
      ByteBuffer nextEncodedBytes = tempBuffer;

      tempBuffer = dataProvider.nextData(readBlockSize);

      nextEncodedBytes.put(tempBuffer);
      nextEncodedBytes.flip();
      return nextEncodedBytes;
   }

   private long getDeltaSizeUpToEndOfTerminationBytes(Charset charset, int lenUpToTermination, String bufferString,
      boolean isFollowUpBlock) {
      long deltaSize = 0;

      int endIndex = bufferString.length();

      if (lenUpToTermination != -1) {
         endIndex = lenUpToTermination + 1;
      }

      deltaSize += bufferString.substring(0, endIndex).getBytes(charset).length;

      byte[] bom = Charsets.getBOM(charset);
      if (isFollowUpBlock && bom != null) {
         deltaSize -= bom.length;
      }
      return deltaSize;
   }
}
