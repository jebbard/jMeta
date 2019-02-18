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
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.FieldDataProvider;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link FieldTerminationFinderTest} tests the {@link FieldTerminationFinder} class. It is parameterized by:
 * <ul>
 * <li>The input string to use</li>
 * <li>The read block size to use</li>
 * <li>The charset to use</li>
 * <li>The expected byte count until (including) the termination character</li>
 * </ul>
 *
 * The tests run with up to two different termination characters.
 */
@RunWith(Parameterized.class)
public class FieldTerminationFinderTest {

   /**
    * {@link TestFieldDataProvider} is a helper {@link FieldDataProvider} that provides binary data from a string.
    */
   private final static class TestFieldDataProvider implements FieldDataProvider {

      private int currentOffset = 0;
      private final byte[] fieldBytes;

      /**
       * Creates a new {@link FieldTerminationFinderTest.TestFieldDataProvider}.
       */
      public TestFieldDataProvider(String fieldString, Charset charset) {
         fieldBytes = fieldString.getBytes(charset);
      }

      /**
       * @see com.github.jmeta.library.datablocks.impl.FieldTerminationFinder.FieldDataProvider#nextData(int)
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

   @Parameters
   public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
         /* String containing only US-ASCII characters, US-ASCII encoding, varying block sizes */
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 100, Charsets.CHARSET_ASCII, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 10, Charsets.CHARSET_ASCII, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 23, Charsets.CHARSET_ASCII, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 201, Charsets.CHARSET_ASCII, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 300, Charsets.CHARSET_ASCII, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 1111, Charsets.CHARSET_ASCII, 281 },
         /* String containing only US-ASCII characters, UTF-16 encoding, varying block sizes */
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 100, Charsets.CHARSET_UTF16, 564 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 12, Charsets.CHARSET_UTF16, 564 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 23, Charsets.CHARSET_UTF16, 564 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 200, Charsets.CHARSET_UTF16, 564 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 303, Charsets.CHARSET_UTF16, 564 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 1111, Charsets.CHARSET_UTF16, 564 },
         /* String containing only US-ASCII characters, UTF-8 encoding, varying block sizes */
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 100, Charsets.CHARSET_UTF8, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 10, Charsets.CHARSET_UTF8, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 27, Charsets.CHARSET_UTF8, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 205, Charsets.CHARSET_UTF8, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 300, Charsets.CHARSET_UTF8, 281 },
         { (Function<Character, String>) FieldTerminationFinderTest::asciiString, 1222, Charsets.CHARSET_UTF8, 281 },
         /* String containing also NON-US-ASCII characters, UTF-16 encoding, varying block sizes */
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 100, Charsets.CHARSET_UTF16, 98 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 12, Charsets.CHARSET_UTF16, 98 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 33, Charsets.CHARSET_UTF16, 98 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 210, Charsets.CHARSET_UTF16, 98 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 303, Charsets.CHARSET_UTF16, 98 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 999, Charsets.CHARSET_UTF16, 98 },
         /*
          * String containing also NON-US-ASCII characters, UTF-8 encoding, varying block sizes
          */
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 100, Charsets.CHARSET_UTF8, 72 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 12, Charsets.CHARSET_UTF8, 72 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 33, Charsets.CHARSET_UTF8, 72 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 210, Charsets.CHARSET_UTF8, 72 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 303, Charsets.CHARSET_UTF8, 72 },
         { (Function<Character, String>) FieldTerminationFinderTest::nonAsciiString, 999, Charsets.CHARSET_UTF8, 72 },

      });
   }

   private final static Character TERMINATION_CHARACTER_1 = '\u0000';
   private final static Character TERMINATION_CHARACTER_2 = '\u0022';

   private Function<Character, String> inputStringProviderFunc;
   private int readBlockSize;
   private Charset charset;
   private int expectedByteCountUpToTermination;

   /**
    * Creates a new {@link FieldTerminationFinderTest}.
    *
    * @param inputStringProviderFunc
    *           A function providing an input test string with the given termination character; passing to it null means
    *           it returns a string without termination character; it returns either a pure US-ASCII string or a string
    *           with some non-US-ASCII characters
    * @param readBlockSize
    *           The block size used for reading
    * @param charset
    *           The {@link Charset} to use
    * @param expectedByteCountUpToTermination
    *           The expected length returned by
    *           {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)},
    *           if terminated including the termination bytes
    */
   public FieldTerminationFinderTest(Function<Character, String> inputStringProviderFunc, int readBlockSize,
      Charset charset, int expectedByteCountUpToTermination) {
      this.inputStringProviderFunc = inputStringProviderFunc;
      this.readBlockSize = readBlockSize;
      this.charset = charset;
      this.expectedByteCountUpToTermination = expectedByteCountUpToTermination;
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBefore_returnsLimit() {
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(null), 10L, 10L, TERMINATION_CHARACTER_1);
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withoutTerminationWithLimitBehind_returnsTotalSize() {
      String inputString = inputStringProviderFunc.apply(null);

      assertExpectedSizeIsReturned(inputString, inputString.length() + 10000, inputString.getBytes(charset).length,
         TERMINATION_CHARACTER_1);
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withoutTerminationWithoutLimit_returnsTotalSize() {
      String inputString = inputStringProviderFunc.apply(null);

      assertExpectedSizeIsReturned(inputString, DataBlockDescription.UNDEFINED, inputString.getBytes(charset).length,
         TERMINATION_CHARACTER_1);
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBefore_returnsLimit() {
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_1), 10L, 10L,
         TERMINATION_CHARACTER_1);
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_2), 10L, 10L,
         TERMINATION_CHARACTER_2);
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withTerminationWithLimitBehind_returnsByteSizeUpToTermination() {
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_1),
         expectedByteCountUpToTermination + 2, expectedByteCountUpToTermination, TERMINATION_CHARACTER_1);
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_2),
         expectedByteCountUpToTermination + 2, expectedByteCountUpToTermination, TERMINATION_CHARACTER_2);
   }

   /**
    * Tests {@link FieldTerminationFinder#getSizeUntilTermination(Charset, Character, FieldDataProvider, long, int)}.
    */
   @Test
   public void getSizeUpToTermination_withTerminationWithoutLimit_returnsByteSizeUpToTermination() {
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_1),
         DataBlockDescription.UNDEFINED, expectedByteCountUpToTermination, TERMINATION_CHARACTER_1);
      assertExpectedSizeIsReturned(inputStringProviderFunc.apply(TERMINATION_CHARACTER_2),
         DataBlockDescription.UNDEFINED, expectedByteCountUpToTermination, TERMINATION_CHARACTER_2);
   }

   private void assertExpectedSizeIsReturned(String inputString, long limit, long expectedSize,
      Character terminationCharacter) {
      FieldTerminationFinder finder = new FieldTerminationFinder();

      long actualSize = finder.getSizeUntilTermination(charset, terminationCharacter,
         new TestFieldDataProvider(inputString, charset), limit, readBlockSize);

      Assert.assertEquals(expectedSize, actualSize);
   }

   /**
    * Returns a string with pure US-ASCII-only characters, including an optional termination character at a fixed place.
    *
    * @param termination
    *           The termination character to use or null if none to use
    * @return The string with the termination character stuffed in at a fixed offset
    */
   private static String asciiString(Character termination) {
      return "Lorem ipsum dolor sit amet, consetetur sadipscing "
         + "elitr, sed diam nonumy eirmod tempor invidunt ut labore et "
         + "dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. "
         + "Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum "
         + (termination == null ? "" : termination) + " dolor sit amet. Lorem ipsum ";
   }

   /**
    * Returns a string with some non US-ASCII characters, including an optional termination character at a fixed place.
    *
    * @param termination
    *           The termination character to use or null if none to use
    * @return The string with the termination character stuffed in at a fixed offset
    */
   private static String nonAsciiString(Character termination) {
      return "Lorem äöü dolß µ, \u8877\u8876\u8875\u8874" + "elitr, äed üiam \u8877\u1249\u4413äasa\u2211 "
         + (termination == null ? "" : termination) + " dolor \u8877 asds ö ä. \u8877 ";
   }
}
