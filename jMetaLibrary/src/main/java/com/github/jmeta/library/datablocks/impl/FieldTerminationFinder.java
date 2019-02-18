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

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinder} has the sole task to find a given termination character in sequence of bytes that have
 * a specific character encoding and that are provided block-wise. The facts of variable byte-sized characters as well
 * as the block-wise reading of bytes lead to the main complexity: Characters overlapping two byte blocks. The process
 * of providing data bytes is abstracted away into the {@link FieldDataProvider} interface which has the purpose to get
 * data from the actual source, keep track of the current block to get and deliver it in a {@link ByteBuffer} to this
 * class.
 *
 * The life-cycle of this class is: Create one instance if you need to find a concrete termination character at any
 * specific time. However, this class is even stateless such that a single instance can be reused arbitrarily. Keep in
 * mind that this is usually not the case for the {@link FieldDataProvider} instance which e.g. needs to keep track of a
 * current position in its state.
 */
public class FieldTerminationFinder {

   /**
    * {@link FieldDataProvider} provides data bytes to search a termination character within.
    */
   @FunctionalInterface
   interface FieldDataProvider {

      /**
       * Provides the next sequence of data in the returned {@link ByteBuffer} between its position and limit. If no
       * bytes are available, it returns an "empty" byte buffer with 0 remaining bytes.
       *
       * @param byteCount
       *           The byte count to get, the method might return fewer bytes
       * @return The bytes read, might be empty and contain fewer bytes than requested; a {@link ByteBuffer} with 0
       *         remaining bytes signals the end of the data providing process
       */
      ByteBuffer nextData(int byteCount);
   }

   /**
    * Determines the number of bytes until and including the bytes of a given termination character. The bytes that are
    * searched are provided sequentially and block-wise by the instance of {@link FieldDataProvider} passed to this
    * method. The search process stops if either the termination character is found in the byte sequence delivered by
    * the {@link FieldDataProvider} instance or the {@link FieldDataProvider} does not return any data anymore. The
    * caller is able to provide a third possibility of earlier termination: A limit can be specified that stops the
    * search if no termination character has been found until that number of scanned bytes.
    *
    * @param charset
    *           The character encoding {@link Charset} the bytes returned from the {@link FieldDataProvider} are encoded
    *           in, must not be null
    * @param terminationCharacter
    *           The termination {@link Character} to search for, must not be null
    * @param dataProvider
    *           The {@link FieldDataProvider} returning the bytes to search within sequentially and block-wise, must not
    *           be null
    * @param limit
    *           The limit of the search, pass in {@link Medium#UNKNOWN_LENGTH} to search without limit, must not be
    *           negative or zero
    * @param readBlockSize
    *           The block size of the data bulks requested in each call to the {@link FieldDataProvider}, must not be
    *           negative or zero
    * @return the number of bytes up to and including the specified termination character bytes within the byte sequence
    *         returned by the given {@link FieldDataProvider}; if no termination character is found before a specified
    *         limit, the limit is returned; if no termination character is found before the last byte returned by the
    *         {@link FieldDataProvider}, the overall byte count is returned
    */
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

         sizeUpToEndOfTerminationBytes += getSizeToAddOfCurrentBlock(charset, lenUpToTermination, bufferString,
            isFollowUpBlock);

         if (limit != DataBlockDescription.UNDEFINED && sizeUpToEndOfTerminationBytes >= limit) {
            return limit;
         }
      } while (lenUpToTermination == -1);

      return sizeUpToEndOfTerminationBytes;
   }

   private ByteBuffer getNextEncodedBytes(ByteBuffer previousEncodedBytes, FieldDataProvider dataProvider,
      int readBlockSize) {
      ByteBuffer nextEncodedBytes = ByteBuffer.allocate(readBlockSize + previousEncodedBytes.remaining());
      nextEncodedBytes.put(previousEncodedBytes);

      nextEncodedBytes.put(dataProvider.nextData(readBlockSize));
      nextEncodedBytes.flip();

      return nextEncodedBytes;
   }

   /**
    * Determines the number of bytes to add to the total number of bytes up to termination for the current block.
    *
    * At least of the final block containing the termination character, this unfortunately can only be done by actually
    * converting the string back to bytes again, because we never know which character has which byte length and where
    * exactly the termination character resides..
    *
    * @param charset
    *           The {@link Charset}
    * @param lenUpToTermination
    *           The number of bytes up to a termination character or -1 if no termination found in the block
    * @param bufferString
    *           The buffer string corresponding to the currently processed block
    * @param isFollowUpBlock
    *           true to signal this is a follow-up block, false if the current block is the very first block; this is
    *           important for handling of BOM bytes for some character encodings
    * @return The size to add for the current block
    */
   private long getSizeToAddOfCurrentBlock(Charset charset, int lenUpToTermination, String bufferString,
      boolean isFollowUpBlock) {
      long deltaSize = 0;

      int endIndex = bufferString.length();

      if (lenUpToTermination != -1) {
         endIndex = lenUpToTermination + 1;
      }

      deltaSize += bufferString.substring(0, endIndex).getBytes(charset).length;

      // We need to subtract any BOM bytes that are added by the Java conversion as they were already counted in the
      // first block and must be considered only once
      byte[] bom = Charsets.getBOM(charset);

      if (isFollowUpBlock && bom != null) {
         deltaSize -= bom.length;
      }
      return deltaSize;
   }
}
