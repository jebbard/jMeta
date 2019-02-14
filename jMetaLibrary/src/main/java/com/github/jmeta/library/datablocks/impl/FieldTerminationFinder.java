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

import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinder}
 *
 */
public class FieldTerminationFinder {

   @FunctionalInterface
   interface DataProvider {

      ByteBuffer nextData(int byteCount);
   }

   public final static long NO_LIMIT = -1;

   public long getSizeUntilTermination(Charset charset, Character terminationCharacter, DataProvider dataProvider,
      long limit, int readBlockSize) {

      long sizeUpToEndOfTerminationBytes = 0;

      boolean terminationBytesFound = false;

      ByteBuffer readBytes = null;
      ByteBuffer encodedBytes = ByteBuffer.allocate(readBlockSize);

      while (!terminationBytesFound) {

         int bytesToRead = readBlockSize;

         readBytes = dataProvider.nextData(bytesToRead);

         // // Special handling for InMemoryMedia: They cannot be used for caching, and then there is no EOM Exception,
         // // thus bytesToRead will still be too big, we have to change it, otherwise Unexpected EOM during readBytes
         // if (bytesToRead == 0 || !mediumHasUnknownLength && bytesToRead > remainingMediumBytes) {
         // bytesToRead = (int) remainingMediumBytes;
         // }

         encodedBytes.put(readBytes);

         CharsetDecoder decoder = charset.newDecoder();

         CharBuffer outputBuffer = CharBuffer.allocate(bytesToRead);

         decoder.decode(encodedBytes, outputBuffer, false);

         for (int i = 0; i < outputBuffer.remaining(); i++) {
            char c = outputBuffer.get();

            sizeUpToEndOfTerminationBytes += Charsets.getBytesWithoutBOM("" + c, charset).length;

            if (terminationCharacter.equals(c)) {
               terminationBytesFound = true;
               break;
            }
         }

         if (!terminationBytesFound) {
            readBytes = ByteBuffer.allocate(readBlockSize + encodedBytes.remaining());
            readBytes.put(encodedBytes);
            encodedBytes = readBytes;
         }
      }

      return sizeUpToEndOfTerminationBytes;
   }
}
