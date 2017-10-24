package com.github.jmeta.library.media.api.helper;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;

public class TestMediumUtility {

   /**
    * A test {@link Medium} to be used whenever it does not matter if the medium exists or it does, i.e. this medium is
    * never accessed or relevant in the test, but it must exist. This is a {@link FileMedium} pointing to an existing
    * file.
    */
   public static final FileMedium DUMMY_DEFAULT_TEST_MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE,
      true);

   /**
    * A second test {@link Medium} to be used whenever it does not matter if it exists. This medium is mainly used for
    * negative testing a class with a second but different unrelated medium and checking its behavior.
    */
   public static final InMemoryMedium DUMMY_UNRELATED_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);

   public static MediumRegion createUnCachedMediumRegion(Medium<?> medium, long offset, Integer size) {
      return new MediumRegion(createReference(medium, offset), size);
   }

   public static MediumRegion createCachedMediumRegion(Medium<?> medium, long offset, Integer size,
      int repeatedByteValue) {

      byte[] newRegionBytes = new byte[size];
      Arrays.fill(newRegionBytes, (byte) repeatedByteValue);

      return new MediumRegion(createReference(medium, offset), ByteBuffer.wrap(newRegionBytes));
   }

   public static MediumRegion createCachedMediumRegion(Medium<?> medium, long offset, Integer size) {
      return new MediumRegion(createReference(medium, offset),
         ByteBuffer.wrap(regionBytesFromDistinctOffsetSequence(offset, size)));
   }

   public static MediumReference createReference(Medium<?> medium, long offset) {
      return new StandardMediumReference(medium, offset);
   }

   public static MediumReference createReferenceToDefaultMedium(long offset) {
      return createReference(DUMMY_DEFAULT_TEST_MEDIUM, offset);
   }

   public static MediumRegion createUnCachedMediumRegionOnDefaultMedium(long offset, Integer size) {
      return createUnCachedMediumRegion(DUMMY_DEFAULT_TEST_MEDIUM, offset, size);
   }

   public static MediumRegion createCachedMediumRegionOnDefaultMedium(long offset, Integer size,
      int repeatedByteValue) {
      return createCachedMediumRegion(DUMMY_DEFAULT_TEST_MEDIUM, offset, size);
   }

   public static MediumRegion createCachedMediumRegionOnDefaultMedium(long offset, Integer size) {
      return createCachedMediumRegion(DUMMY_DEFAULT_TEST_MEDIUM, offset, size);
   }

   public static ByteBuffer getPartFromReadOnlyByteBuffer(ByteBuffer byteBuffer, int startIndex, int byteCount) {
      byte[] dividedRegionBytesPartOne = new byte[byteCount];

      for (int i = 0; i < dividedRegionBytesPartOne.length; i++) {
         dividedRegionBytesPartOne[i] = byteBuffer.get(startIndex + i);
      }

      ByteBuffer dividedRegionByteBufferPartOne = ByteBuffer.wrap(dividedRegionBytesPartOne);
      return dividedRegionByteBufferPartOne;
   }

   public static byte[] regionBytesFromFillByte(byte fillByte, int size) {
      byte[] content = new byte[size];

      Arrays.fill(content, fillByte);

      return content;
   }

   public static byte[] regionBytesFromMediumRegion(MediumRegion region) {
      ByteBuffer bytes = region.getBytes();
      byte[] content = new byte[bytes.remaining()];

      bytes.get(content);

      return content;
   }

   public static byte[] regionBytesFromDistinctOffsetSequence(long offset, int size) {
      byte[] content = new byte[size];

      for (int i = 0; i < content.length; i++) {
         content[i] = (byte) ((offset + i) % Byte.MAX_VALUE);
      }

      return content;
   }

   private TestMediumUtility() {

   }
}
