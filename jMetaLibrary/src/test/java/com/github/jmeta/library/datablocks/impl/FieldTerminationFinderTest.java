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
import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.DataProvider;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinderTest} tests the {@link FieldTerminationFinder} class.
 */
@RunWith(Parameterized.class)
public class FieldTerminationFinderTest {

   @Parameters
   public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
         /* String containing only US-ASCII characters, US-ASCII encoding, varying block sizes */
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 100, Charsets.CHARSET_ASCII, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 10, Charsets.CHARSET_ASCII, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 23, Charsets.CHARSET_ASCII, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 201, Charsets.CHARSET_ASCII, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 300, Charsets.CHARSET_ASCII, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 1111, Charsets.CHARSET_ASCII, 281 },
         /* String containing only US-ASCII characters, UTF-16 encoding, varying block sizes */
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 100, Charsets.CHARSET_UTF16, 564 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 12, Charsets.CHARSET_UTF16, 564 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 23, Charsets.CHARSET_UTF16, 564 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 200, Charsets.CHARSET_UTF16, 564 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 303, Charsets.CHARSET_UTF16, 564 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 1111, Charsets.CHARSET_UTF16, 564 },
         /* String containing only US-ASCII characters, UTF-8 encoding, varying block sizes */
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 100, Charsets.CHARSET_UTF8, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 10, Charsets.CHARSET_UTF8, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 27, Charsets.CHARSET_UTF8, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 205, Charsets.CHARSET_UTF8, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 300, Charsets.CHARSET_UTF8, 281 },
         { ASCII_WITH_TERMINATION, ASCII_WITHOUT_TERMINATION, 1222, Charsets.CHARSET_UTF8, 281 },
         /* String containing also NON-US-ASCII characters, UTF-16 encoding, varying block sizes */
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 100, Charsets.CHARSET_UTF16, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 12, Charsets.CHARSET_UTF16, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 33, Charsets.CHARSET_UTF16, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 210, Charsets.CHARSET_UTF16, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 303, Charsets.CHARSET_UTF16, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 999, Charsets.CHARSET_UTF16, 98 },
         /* String containing also NON-US-ASCII characters, UTF-8 encoding, varying block sizes */
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 100, Charsets.CHARSET_UTF8, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 12, Charsets.CHARSET_UTF8, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 33, Charsets.CHARSET_UTF8, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 210, Charsets.CHARSET_UTF8, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 303, Charsets.CHARSET_UTF8, 98 },
         { NON_ASCII_WITH_TERMINATION, NON_ASCII_WITHOUT_TERMINATION, 999, Charsets.CHARSET_UTF8, 98 },

      });
   }

   private String stringWithTermination;
   private String stringWithoutTermination;
   private int readBlockSize;
   private Charset charset;
   private int expectedByteCountUpToTermination;

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

         ByteBuffer resultBuffer = ByteBuffer.wrap(fieldBytes, currentOffset, byteCount);

         currentOffset += byteCount;
         return resultBuffer;
      }
   }

   /**
    * Creates a new {@link FieldTerminationFinderTest}.
    *
    * @param stringWithTermination
    * @param stringWithoutTermination
    * @param readBlockSize
    * @param charset
    * @param expectedByteCountUpToTermination
    */
   public FieldTerminationFinderTest(String stringWithTermination, String stringWithoutTermination, int readBlockSize,
      Charset charset, int expectedByteCountUpToTermination) {
      super();
      this.stringWithTermination = stringWithTermination;
      this.stringWithoutTermination = stringWithoutTermination;
      this.readBlockSize = readBlockSize;
      this.charset = charset;
      this.expectedByteCountUpToTermination = expectedByteCountUpToTermination;
   }

   private final static Character TERMINATION_CHARACTER = '\u0000';

   private final static String ASCII_WITH_TERMINATION = "Lorem ipsum dolor sit amet, consetetur sadipscing "
      + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et "
      + "dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
      + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum " + TERMINATION_CHARACTER
      + " dolor sit amet. Lorem ipsum ";

   private final static String ASCII_WITHOUT_TERMINATION = "Lorem ipsum dolor sit amet, consetetur "
      + "sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et "
      + "dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
      + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";

   private final static String NON_ASCII_WITH_TERMINATION = "Lorem äöü dolß µ, \u8877\u8876\u8875\u8874"
      + "elitr, äed üiam \u8877\u1249\u4413äasa\u2211 " + TERMINATION_CHARACTER + " dolor \u8877 asds ö ä. \u8877 ";

   private final static String NON_ASCII_WITHOUT_TERMINATION = "Lorem äöü dolß µ, \u8877\u8876\u8875\u8874"
      + "elitr, äed üiam \u8877\u1249\u4413äasa\u2211  dolor \u8877 asds ö ä. \u8877 ";

   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBefore_returnsLimit() {
      assertLengthEqualsLimit(stringWithoutTermination, charset, readBlockSize, 10L);
   }

   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBehind_returnsTotalSize() {
      assertLengthEqualsTotalSize(stringWithoutTermination, charset, readBlockSize,
         stringWithoutTermination.length() + 10000);
   }

   @Test
   public void getSizeUpToTermination_withoutTerminationWithoutLimit_returnsTotalSize() {
      assertLengthEqualsTotalSize(stringWithoutTermination, charset, readBlockSize, FieldTerminationFinder.NO_LIMIT);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBefore_returnsLimit() {
      assertLengthEqualsLimit(stringWithTermination, charset, readBlockSize, 10L);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBehind_returnsByteSizeUpToTermination() {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      long actualSize = finder.getSizeUntilTermination(charset, TERMINATION_CHARACTER,
         new TestFieldDataProvider(stringWithTermination, charset), 1000L, readBlockSize);

      Assert.assertEquals(expectedByteCountUpToTermination, actualSize);
   }

   @Test
   public void getSizeUpToTermination_withTerminationWithoutLimit_returnsByteSizeUpToTermination() {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      long actualSize = finder.getSizeUntilTermination(charset, TERMINATION_CHARACTER,
         new TestFieldDataProvider(stringWithTermination, charset), FieldTerminationFinder.NO_LIMIT, readBlockSize);

      Assert.assertEquals(expectedByteCountUpToTermination, actualSize);
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
