/**
 *
 * MediaTestHelper.java
 *
 * @author Jens
 *
 * @date 16.10.2016
 *
 */
package com.github.jmeta.library.media.api.helper;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO dissolve this class, use TestMediumHelper
/**
 * {@link MediaTestHelper}
 *
 */
public class MediaTestHelper {

   private final static ByteBuffer THE_EMPTY_BUFFER = ByteBuffer.allocate(0);
   private final static FileMedium STANDARD_MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

   /**
    * Creates a new {@link MediaTestHelper}.
    */
   private MediaTestHelper() {
   }

   /**
    * Convenience method returning a new {@link MediumReference} pointing to the specified offset on the default
    * {@value #STANDARD_MEDIUM}.
    * 
    * @param offset
    *           The offset to use.
    * @return a new {@link MediumReference} pointing to the specified offset on the default {@value #STANDARD_MEDIUM}.
    */
   public static MediumReference at(long offset) {

      return new StandardMediumReference(STANDARD_MEDIUM, offset);
   }

   /**
    * @return a standard dummy {@link Medium} with no specific properties for testing
    */
   public static Medium<?> getStandardMedium() {
      return STANDARD_MEDIUM;
   }

   /**
    * Convenience version of {@link #createTestByteBufferOfSize(int, byte)}, starting at byte offset 0.
    * 
    * @param size
    *           The total size of the buffer to create. Must be positive. If this parameter is 0, the empty
    *           {@link ByteBuffer} is returned.
    * @return see {@link #createTestByteBufferOfSize(int, byte)}.
    */
   public static ByteBuffer createTestByteBufferOfSize(int size) {
      return createTestByteBufferOfSize(size, (byte) 0);
   }

   /**
    * Creates a test byte buffer of the given size, filled with increasing byte values starting from the given start
    * offset. The first goal of this method is to provide {@link ByteBuffer}s with non-uniform content (e.g. not only
    * filled with zeroes). This ensures that during testing the correct portions of a ByteBuffer are checked against, so
    * it avoids bugs. If you would instead always use {@link ByteBuffer} filled uniformly with the same byte, you would
    * e.g. not find bugs related to wrongly copying contents of the buffer. The second goal of this method is to yet
    * provide reproducible data. Thus it does not simply return a random content buffer, but you always know what you
    * get, and subsequent calls with the same parameters return always the same {@link ByteBuffer}.
    * 
    * @param size
    *           The total size of the buffer to create. Must be positive. If this parameter is 0, the empty
    *           {@link ByteBuffer} is returned.
    * @param startByteOffset
    *           The start offset at which each byte sequence starts. Must be smaller than {@link Byte#MAX_VALUE}.
    * @return A test {@link ByteBuffer} of the given size, filled with size / (Byte.MAX_VALUE - startByteOffset)
    *         identical blocks of Bytes at front, each a sequence of length Byte.MAX_VALUE - startByteOffset, that
    *         starts with startByteOffset as the first byte, and continues with startByteOffset+1 as second byte and so
    *         on. At the end, there are size % (Byte.MAX_VALUE - startByteOffset) bytes that follow the same sequence.
    */
   public static ByteBuffer createTestByteBufferOfSize(int size, byte startByteOffset) {
      Reject.ifTrue(size < 0, "size < 0");
      Reject.ifNotInInterval(startByteOffset, 0, Byte.MAX_VALUE - 1, "startByteOffset");

      if (size == 0) {
         return THE_EMPTY_BUFFER;
      }

      int blockSize = Byte.MAX_VALUE - startByteOffset;
      int blockCount = size / blockSize;
      int remainder = size % blockSize;

      List<Byte> totalByteSequence = new ArrayList<>();

      for (int j = 0; j < blockCount; j++) {
         List<Byte> bytes = IntStream.rangeClosed(startByteOffset, blockSize - 1).mapToObj(i -> (byte) i)
            .collect(Collectors.toList());

         totalByteSequence.addAll(bytes);
      }

      totalByteSequence.addAll(
         IntStream.rangeClosed(startByteOffset, remainder - 1).mapToObj(i -> (byte) i).collect(Collectors.toList()));

      return ByteBuffer.wrap(ByteArrayUtils.toArray(totalByteSequence));
   }

}
