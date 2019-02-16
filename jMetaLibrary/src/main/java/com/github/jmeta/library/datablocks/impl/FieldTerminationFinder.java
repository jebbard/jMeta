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
         encodedBytes.flip();

         if (!encodedBytes.hasRemaining()) {
            return sizeUpToEndOfTerminationBytes;
         }

         int byteCountBeforeDecode = encodedBytes.remaining();

         CharsetDecoder decoder = charset.newDecoder();

         CharBuffer outputBuffer = CharBuffer.allocate(byteCountBeforeDecode);
         CoderResult result = decoder.decode(encodedBytes, outputBuffer, false);

         outputBuffer.flip();

         if (limit != NO_LIMIT && sizeUpToEndOfTerminationBytes + outputBuffer.remaining() > limit) {
            return limit;
         }

         String bufferString = outputBuffer.toString();

         int lenUpToTermination = bufferString.indexOf(terminationCharacter);

         int endIndex = outputBuffer.remaining();

         if (lenUpToTermination != -1) {
            endIndex = lenUpToTermination + 1;
            terminationBytesFound = true;
         }

         boolean isFollowUpBlock = sizeUpToEndOfTerminationBytes > 0;

         if (true/* charset.equals(Charsets.CHARSET_UTF16) && lenUpToTermination != -1 */) {
            sizeUpToEndOfTerminationBytes += bufferString.substring(0, endIndex).getBytes(charset).length;
            // } else {
            //
            // for (int i = 0; i < remaining; i++) {
            // char c = outputBuffer.get();
            //
            // sizeUpToEndOfTerminationBytes += Charsets.getBytesWithoutBOM("" + c, charset).length;
            //
            // if (terminationCharacter.equals(c)) {
            // terminationBytesFound = true;
            // break;
            // }
            // }
         }

         if (isFollowUpBlock && Charsets.hasBOM(charset)) {
            sizeUpToEndOfTerminationBytes -= 2;
         }

         if (!terminationBytesFound) {

            if (limit != NO_LIMIT && sizeUpToEndOfTerminationBytes >= limit) {
               return limit;
            }

            readBytes = ByteBuffer.allocate(readBlockSize + encodedBytes.remaining());
            readBytes.put(encodedBytes);
            encodedBytes = readBytes;
         }
      }

      return sizeUpToEndOfTerminationBytes;
   }
}
