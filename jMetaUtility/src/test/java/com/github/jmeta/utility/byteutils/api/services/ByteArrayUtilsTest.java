/**
 *
 * {@link ByteArrayUtilsTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2009
 *
 */
package com.github.jmeta.utility.byteutils.api.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.utility.byteutils.api.exceptions.InvalidArrayStringFormatException;

/**
 * Tests the {@link ByteArrayUtils} class.
 */
public class ByteArrayUtilsTest {

   private final List<Map<Integer, byte[]>> arraysToFind = new ArrayList<>();
   private final List<Map<Integer, byte[]>> arraysToFindOfs = new ArrayList<>();
   private final List<Map<Integer, Integer>> offsetReturnValueMapping = new ArrayList<>();
   private final List<byte[]> arraysToSearchIn = new ArrayList<>();
   private final List<byte[]> arraysToMerge = arraysToSearchIn;
   private byte[] mergedArray;
   private final Map<String, byte[]> stringArraysCorrect = new HashMap<>();
   private final List<String> stringArraysIncorrect = new ArrayList<>();

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      arraysToSearchIn.add(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      arraysToSearchIn.add(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
      arraysToSearchIn.add(new byte[] { 9 });
      arraysToSearchIn.add(new byte[] { 1, 2, 3, 34, -22, 36, 23, 0, 0, 5, 6, 0, 0 });

      mergedArray = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 9, 1, 2, 3, 34, -22, 36, 23, 0, 0, 5,
         6, 0, 0 };

      arraysToFind.add(new HashMap<Integer, byte[]>());
      arraysToFind.get(0).put(0, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      arraysToFind.get(0).put(1, new byte[] { 1, 2 });
      arraysToFind.get(0).put(6, new byte[] { 6, 7 });
      arraysToFind.get(0).put(-1, new byte[] { 1, 2, 4 });
      arraysToFind.get(0).put(5, new byte[] { 5 });

      arraysToFind.add(new HashMap<Integer, byte[]>());
      arraysToFind.get(1).put(0, new byte[] { 0, 0, 0, 0, 0, 0 });
      arraysToFind.get(1).put(-1, new byte[] { 0, 1, 0 });

      arraysToFind.add(new HashMap<Integer, byte[]>());
      arraysToFind.get(2).put(0, new byte[] { 9 });
      arraysToFind.get(2).put(-1, new byte[] { -1 });

      arraysToFind.add(new HashMap<Integer, byte[]>());
      arraysToFind.get(3).put(4, new byte[] { -22, 36, 23, 0 });
      arraysToFind.get(3).put(7, new byte[] { 0, 0 });
      arraysToFind.get(3).put(0, new byte[] { 1, 2, 3 });
      arraysToFind.get(3).put(-1, new byte[] { -22, 36, 23, 0, 0, 5, 7 });

      arraysToFindOfs.add(new HashMap<Integer, byte[]>());
      arraysToFindOfs.get(0).put(-1, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      arraysToFindOfs.get(0).put(5, new byte[] { 5, 6 });

      offsetReturnValueMapping.add(new HashMap<Integer, Integer>());
      offsetReturnValueMapping.get(0).put(-1, 3);
      offsetReturnValueMapping.get(0).put(5, 2);

      // Test data for parseArray
      stringArraysCorrect.put("{1,2,3,}", new byte[] { 1, 2, 3 });
      stringArraysCorrect.put("{1, 2,3,}", new byte[] { 1, 2, 3 });
      stringArraysCorrect.put("{-123,}", new byte[] { -123 });
      stringArraysCorrect.put("{-123,1,5, 10, -32, 5,}", new byte[] { -123, 1, 5, 10, -32, 5 });
      stringArraysCorrect.put("{-123,1,5, 10, -32, 5,}*9",
         new byte[] { -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5,
            -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5,
            10, -32, 5, });
      stringArraysCorrect.put("{1,}", new byte[] { 1 });
      stringArraysCorrect.put("{1,}*1", new byte[] { 1 });
      stringArraysCorrect.put("{1,}*4", new byte[] { 1, 1, 1, 1 });
      stringArraysCorrect.put("{1,2,3,}*2", new byte[] { 1, 2, 3, 1, 2, 3 });
      stringArraysCorrect.put("{}", new byte[] {});
      stringArraysCorrect.put("{}*4", new byte[] {});

      stringArraysIncorrect.add("{1,2,3}");
      stringArraysIncorrect.add("{234, 2,3,}");
      stringArraysIncorrect.add("{--123,}");
      stringArraysIncorrect.add("{xa}");
      stringArraysIncorrect.add("{,}");
      stringArraysIncorrect.add("{'a',}");
      stringArraysIncorrect.add("{-123,1,5, 10, -32, 5,");
      stringArraysIncorrect.add("-123,1,5}");
      stringArraysIncorrect.add("{+123,1,5}");
      stringArraysIncorrect.add("{1,  2,}");
      stringArraysIncorrect.add("{1, 2,}*");
      stringArraysIncorrect.add("{1, 2,}*-1");
      stringArraysIncorrect.add("{1, 2,}*0");
      stringArraysIncorrect.add("{1, 2,}*214748364700");
   }

   /**
    * Test {@link ByteArrayUtils#findFirst(byte[], byte[], int, int)} w/o start offset.
    */
   @Test
   public void test_findFirstWOStartOffset() {
      for (int i = 0; i < arraysToSearchIn.size(); ++i) {
         byte[] bytes = arraysToSearchIn.get(i);

         Map<Integer, byte[]> returnValueSearchedBytesMapping = arraysToFind.get(i);

         for (Integer expectedReturnValue : returnValueSearchedBytesMapping.keySet())
            Assert.assertEquals(expectedReturnValue.intValue(),
               ByteArrayUtils.findFirst(bytes, returnValueSearchedBytesMapping.get(expectedReturnValue), 0, 1));
      }
   }

   /**
    * Test {@link ByteArrayUtils#findFirst(byte[], byte[], int, int)} with start offset.
    */
   @Test
   public void test_findFirstWithStartOffset() {
      for (int i = 0; i < arraysToFindOfs.size(); ++i) {
         byte[] bytes = arraysToSearchIn.get(i);

         Map<Integer, byte[]> returnValueSearchedBytesMapping = arraysToFindOfs.get(i);

         for (Integer expectedReturnValue : returnValueSearchedBytesMapping.keySet())
            Assert.assertEquals(expectedReturnValue.intValue(),
               ByteArrayUtils.findFirst(bytes, returnValueSearchedBytesMapping.get(expectedReturnValue),
                  offsetReturnValueMapping.get(i).get(expectedReturnValue), 1));
      }
   }

   /**
    * Test precondition of {@link ByteArrayUtils#merge(List, int)}.
    */
   @Test
   public void test_merge() {
      int totalLength = 0;

      for (byte[] bytes : arraysToMerge)
         totalLength += bytes.length;

      Assert.assertArrayEquals(mergedArray, ByteArrayUtils.merge(arraysToMerge, totalLength));
   }

   /**
    * Tests {@link ByteArrayUtils#parseArray(String)}.
    */
   @Test
   public void test_parseArray() {
      // Test positive case
      for (Iterator<String> iterator = stringArraysCorrect.keySet().iterator(); iterator.hasNext();) {
         String parseExpression = iterator.next();
         byte[] expectedByteArray = stringArraysCorrect.get(parseExpression);
         byte[] parsedByteArray = null;

         try {
            parsedByteArray = ByteArrayUtils.parseArray(parseExpression);
         } catch (InvalidArrayStringFormatException e) {
            Assert.fail("Unexpected exception" + e);
         }

         org.junit.Assert.assertArrayEquals(expectedByteArray, parsedByteArray);
      }

      // Test negative case
      for (int i = 0; i < stringArraysIncorrect.size(); ++i) {
         String incorrectParseExpression = stringArraysIncorrect.get(i);

         try {
            ByteArrayUtils.parseArray(incorrectParseExpression);
            Assert.fail("Unexpected exception " + InvalidArrayStringFormatException.class.getName());
         } catch (InvalidArrayStringFormatException e) {
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }
}
