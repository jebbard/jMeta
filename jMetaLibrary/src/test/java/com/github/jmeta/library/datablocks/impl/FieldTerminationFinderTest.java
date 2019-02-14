/**
 *
 * {@link FieldTerminationFinderTest}.java
 *
 * @author Jens Ebert
 *
 * @date 14.02.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.DataProvider;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinderTest} tests the {@link FieldTerminationFinder} class.
 */
public class FieldTerminationFinderTest {

   private final static class TestFieldDataProvider implements DataProvider {

      private int currentOffset = 0;
      private final byte[] fieldBytes;

      /**
       * Creates a new {@link FieldTerminationFinderTest.TestFieldDataProvider}.
       */
      public TestFieldDataProvider(String fieldString, Charset charset) {
         fieldBytes = fieldString.getBytes(charset);
      }

      /**
       * @see com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.DataProvider#nextData(int)
       */
      @Override
      public ByteBuffer nextData(int byteCount) {

         if (currentOffset + byteCount > fieldBytes.length) {
            byteCount = fieldBytes.length - currentOffset;
         }

         return ByteBuffer.wrap(fieldBytes, currentOffset, byteCount);
      }
   }

   private final static Character TERMINATION_CHARACTER = '\u0000';

   private final static String TEST_FIELD_STRING_WITH_TERMINATION = "Lorem ipsum dolor sit amet, consetetur sadipscing "
      + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et "
      + "dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
      + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " + TERMINATION_CHARACTER
      + " dolor sit amet. Lorem ipsum ";

   private final static String TEST_FIELD_STRING_WITHOUT_TERMINATION = "Lorem ipsum dolor sit amet, consetetur "
      + "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et "
      + "dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
      + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBefore_returnsLimit() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsLimit(TEST_FIELD_STRING_WITHOUT_TERMINATION, charset, readBlockSize, 10L);
   }

   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBehind_returnsTotalSize() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsTotalSize(TEST_FIELD_STRING_WITHOUT_TERMINATION, charset, readBlockSize,
         TEST_FIELD_STRING_WITHOUT_TERMINATION.length() + 100);
   }

   @Test
   public void getSizeUpToTermination_withoutTerminationWithoutLimit_returnsTotalSize() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsTotalSize(TEST_FIELD_STRING_WITHOUT_TERMINATION, charset, readBlockSize,
         FieldTerminationFinder.NO_LIMIT);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBefore_returnsLimit() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsLimit(TEST_FIELD_STRING_WITH_TERMINATION, charset, readBlockSize, 10L);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBehind_returnsByteSizeUpToTermination() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsLimit(TEST_FIELD_STRING_WITH_TERMINATION, charset, readBlockSize, 1000L);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithoutLimit_returnsByteSizeUpToTermination() {
      int readBlockSize = 100;
      Charset charset = Charsets.CHARSET_ASCII;

      assertLengthEqualsLimit(TEST_FIELD_STRING_WITH_TERMINATION, charset, readBlockSize,
         FieldTerminationFinder.NO_LIMIT);
   }

   private void assertLengthEqualsLimit(String inputData, Charset charset, int readBlockSize, long limit) {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      long actualSize = finder.getSizeUntilTermination(charset, TERMINATION_CHARACTER,
         new TestFieldDataProvider(inputData, charset), limit, readBlockSize);

      Assert.assertEquals(limit, actualSize);
   }

   private void assertLengthEqualsTotalSize(String inputData, Charset charset, int readBlockSize, long limit) {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      long actualSize = finder.getSizeUntilTermination(charset, TERMINATION_CHARACTER,
         new TestFieldDataProvider(inputData, charset), limit, readBlockSize);

      Assert.assertEquals(inputData.getBytes(charset).length, actualSize);
   }
}
