/**
 *
 * {@link EnhancedArraysXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2009
 *
 */
package de.je.util.javautil.common.array;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * {@link EnhancedArraysXXXTest} tests the {@link EnhancedArrays} class.
 */
public class EnhancedArraysXXXTest {

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      m_arraysToSearchIn.add(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      m_arraysToSearchIn.add(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 });
      m_arraysToSearchIn.add(new byte[] { 9 });
      m_arraysToSearchIn.add(new byte[] { 1, 2, 3, 34, -22, 36, 23, 0, 0, 5, 6, 0, 0 });

      m_mergedArray = new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 0, 0, 0, 0, 0, 0, 0, 0, 9, 1, 2, 3, 34, -22, 36, 23, 0, 0, 5,
         6, 0, 0 };

      m_arraysToFind.add(new HashMap<Integer, byte[]>());
      m_arraysToFind.get(0).put(0, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      m_arraysToFind.get(0).put(1, new byte[] { 1, 2 });
      m_arraysToFind.get(0).put(6, new byte[] { 6, 7 });
      m_arraysToFind.get(0).put(-1, new byte[] { 1, 2, 4 });
      m_arraysToFind.get(0).put(5, new byte[] { 5 });

      m_arraysToFind.add(new HashMap<Integer, byte[]>());
      m_arraysToFind.get(1).put(0, new byte[] { 0, 0, 0, 0, 0, 0 });
      m_arraysToFind.get(1).put(-1, new byte[] { 0, 1, 0 });

      m_arraysToFind.add(new HashMap<Integer, byte[]>());
      m_arraysToFind.get(2).put(0, new byte[] { 9 });
      m_arraysToFind.get(2).put(-1, new byte[] { -1 });

      m_arraysToFind.add(new HashMap<Integer, byte[]>());
      m_arraysToFind.get(3).put(4, new byte[] { -22, 36, 23, 0 });
      m_arraysToFind.get(3).put(7, new byte[] { 0, 0 });
      m_arraysToFind.get(3).put(0, new byte[] { 1, 2, 3 });
      m_arraysToFind.get(3).put(-1, new byte[] { -22, 36, 23, 0, 0, 5, 7 });

      m_arraysToFindOfs.add(new HashMap<Integer, byte[]>());
      m_arraysToFindOfs.get(0).put(-1, new byte[] { 0, 1, 2, 3, 4, 5, 6, 7 });
      m_arraysToFindOfs.get(0).put(5, new byte[] { 5, 6 });

      m_offsetReturnValueMapping.add(new HashMap<Integer, Integer>());
      m_offsetReturnValueMapping.get(0).put(-1, 3);
      m_offsetReturnValueMapping.get(0).put(5, 2);

      // Test data for parseArray
      m_stringArraysCorrect.put("{1,2,3,}", new byte[] { 1, 2, 3 });
      m_stringArraysCorrect.put("{1, 2,3,}", new byte[] { 1, 2, 3 });
      m_stringArraysCorrect.put("{-123,}", new byte[] { -123 });
      m_stringArraysCorrect.put("{-123,1,5, 10, -32, 5,}", new byte[] { -123, 1, 5, 10, -32, 5 });
      m_stringArraysCorrect.put("{-123,1,5, 10, -32, 5,}*9",
         new byte[] { -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5,
            -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5, 10, -32, 5, -123, 1, 5,
            10, -32, 5, });
      m_stringArraysCorrect.put("{1,}", new byte[] { 1 });
      m_stringArraysCorrect.put("{1,}*1", new byte[] { 1 });
      m_stringArraysCorrect.put("{1,}*4", new byte[] { 1, 1, 1, 1 });
      m_stringArraysCorrect.put("{1,2,3,}*2", new byte[] { 1, 2, 3, 1, 2, 3 });
      m_stringArraysCorrect.put("{}", new byte[] {});
      m_stringArraysCorrect.put("{}*4", new byte[] {});

      m_stringArraysIncorrect.add("{1,2,3}");
      m_stringArraysIncorrect.add("{234, 2,3,}");
      m_stringArraysIncorrect.add("{--123,}");
      m_stringArraysIncorrect.add("{xa}");
      m_stringArraysIncorrect.add("{,}");
      m_stringArraysIncorrect.add("{'a',}");
      m_stringArraysIncorrect.add("{-123,1,5, 10, -32, 5,");
      m_stringArraysIncorrect.add("-123,1,5}");
      m_stringArraysIncorrect.add("{+123,1,5}");
      m_stringArraysIncorrect.add("{1,  2,}");
      m_stringArraysIncorrect.add("{1, 2,}*");
      m_stringArraysIncorrect.add("{1, 2,}*-1");
      m_stringArraysIncorrect.add("{1, 2,}*0");
      m_stringArraysIncorrect.add("{1, 2,}*214748364700");
   }

   /**
    * Test {@link EnhancedArrays#findFirst(byte[], byte[], int, int)} w/o start offset.
    */
   @Test
   public void test_findFirstWOStartOffset() {
      for (int i = 0; i < m_arraysToSearchIn.size(); ++i) {
         byte[] bytes = m_arraysToSearchIn.get(i);

         Map<Integer, byte[]> returnValueSearchedBytesMapping = m_arraysToFind.get(i);

         for (Integer expectedReturnValue : returnValueSearchedBytesMapping.keySet())
            Assert.assertEquals(expectedReturnValue.intValue(),
               EnhancedArrays.findFirst(bytes, returnValueSearchedBytesMapping.get(expectedReturnValue), 0, 1));
      }
   }

   /**
    * Test {@link EnhancedArrays#findFirst(byte[], byte[], int, int)} with start offset.
    */
   @Test
   public void test_findFirstWithStartOffset() {
      for (int i = 0; i < m_arraysToFindOfs.size(); ++i) {
         byte[] bytes = m_arraysToSearchIn.get(i);

         Map<Integer, byte[]> returnValueSearchedBytesMapping = m_arraysToFindOfs.get(i);

         for (Integer expectedReturnValue : returnValueSearchedBytesMapping.keySet())
            Assert.assertEquals(expectedReturnValue.intValue(),
               EnhancedArrays.findFirst(bytes, returnValueSearchedBytesMapping.get(expectedReturnValue),
                  m_offsetReturnValueMapping.get(i).get(expectedReturnValue), 1));
      }
   }

   /**
    * Test precondition of {@link EnhancedArrays#merge(List, int)}.
    */
   @Test
   public void test_merge() {
      int totalLength = 0;

      for (byte[] bytes : m_arraysToMerge)
         totalLength += bytes.length;

      Assert.assertArrayEquals(m_mergedArray, EnhancedArrays.merge(m_arraysToMerge, totalLength));
   }

   /**
    * Tests {@link EnhancedArrays#parseArray(String)}.
    */
   @Test
   public void test_parseArray() {
      // Test positive case
      for (Iterator<String> iterator = m_stringArraysCorrect.keySet().iterator(); iterator.hasNext();) {
         String parseExpression = iterator.next();
         byte[] expectedByteArray = m_stringArraysCorrect.get(parseExpression);
         byte[] parsedByteArray = null;

         try {
            parsedByteArray = EnhancedArrays.parseArray(parseExpression);
         } catch (InvalidArrayStringFormatException e) {
            Assert.fail("Unexpected exception" + e);
         }

         org.junit.Assert.assertArrayEquals(expectedByteArray, parsedByteArray);
      }

      // Test negative case
      for (int i = 0; i < m_stringArraysIncorrect.size(); ++i) {
         String incorrectParseExpression = m_stringArraysIncorrect.get(i);

         try {
            EnhancedArrays.parseArray(incorrectParseExpression);
            Assert.fail("Unexpected exception " + InvalidArrayStringFormatException.class.getName());
         } catch (InvalidArrayStringFormatException e) {
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }

   private final List<Map<Integer, byte[]>> m_arraysToFind = new ArrayList<>();
   private final List<Map<Integer, byte[]>> m_arraysToFindOfs = new ArrayList<>();
   private final List<Map<Integer, Integer>> m_offsetReturnValueMapping = new ArrayList<>();
   private final List<byte[]> m_arraysToSearchIn = new ArrayList<>();
   private final List<byte[]> m_arraysToMerge = m_arraysToSearchIn;
   private byte[] m_mergedArray;

   private final Map<String, byte[]> m_stringArraysCorrect = new HashMap<>();
   private final List<String> m_stringArraysIncorrect = new ArrayList<>();
}
