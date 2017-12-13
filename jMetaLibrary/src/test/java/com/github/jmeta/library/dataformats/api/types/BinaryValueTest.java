package com.github.jmeta.library.dataformats.api.types;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link BinaryValueTest} tests the {@link BinaryValue} class.
 */
public class BinaryValueTest {

   /**
    * Tests {@link BinaryValue#getBytes(long, int)}, {@link BinaryValue#getFragmentCount()},
    * {@link BinaryValue#getFragment(int)}, {@link BinaryValue#getFragmentForOffset(long)} and
    * {@link BinaryValue#getTotalSize()}.
    */
   @Test
   public void testAll() {

      for (Iterator<byte[]> iterator = TEST_ARRAYS.keySet().iterator(); iterator.hasNext();) {
         byte[] expectedBytes = iterator.next();
         int fragmentSize = TEST_ARRAYS.get(expectedBytes);

         BinaryValue binaryValue = new BinaryValue(expectedBytes, fragmentSize);

         Assert.assertEquals(expectedBytes.length, binaryValue.getTotalSize());

         int expectedFragmentCount = 0;

         if (expectedBytes.length > 0) {
            org.junit.Assert.assertArrayEquals(expectedBytes, binaryValue.getBytes(0, expectedBytes.length));

            expectedFragmentCount = expectedBytes.length / binaryValue.getMaxFragmentSize()
               + ((expectedBytes.length % binaryValue.getMaxFragmentSize()) != 0 ? 1 : 0);
         }

         Assert.assertEquals(expectedFragmentCount, binaryValue.getFragmentCount());

         long currentOffset = 0;

         for (int i = 0; i < expectedFragmentCount; i++) {
            byte[] fragment = binaryValue.getFragment(i);
            byte[] fragmentGet = binaryValue.getBytes(currentOffset, fragment.length);

            org.junit.Assert.assertArrayEquals(fragment, fragmentGet);
            org.junit.Assert.assertArrayEquals(fragment, binaryValue.getFragmentForOffset(currentOffset));
            org.junit.Assert.assertArrayEquals(fragmentGet, binaryValue.getFragmentForOffset(currentOffset));
            currentOffset += fragment.length;
         }
      }
   }

   private static final int SMALLER_FRAGMENT_SIZE = 11;

   private static final Map<byte[], Integer> TEST_ARRAYS = new HashMap<>();

   static {
      TEST_ARRAYS.put(new byte[] {}, Integer.MAX_VALUE);
      TEST_ARRAYS.put(new byte[] { 0 }, Integer.MAX_VALUE);
      TEST_ARRAYS.put(new byte[] { 1, 2 }, Integer.MAX_VALUE);
      TEST_ARRAYS.put(new byte[] {}, SMALLER_FRAGMENT_SIZE);
      TEST_ARRAYS.put(new byte[] { 0 }, SMALLER_FRAGMENT_SIZE);
      TEST_ARRAYS.put(new byte[] { 1, 2 }, SMALLER_FRAGMENT_SIZE);
      /* One bigger than max fragment size */
      TEST_ARRAYS.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12 }, SMALLER_FRAGMENT_SIZE);
      /* Exactly the max fragment size */
      TEST_ARRAYS.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 }, SMALLER_FRAGMENT_SIZE);
      /* Twice the max fragment size */
      TEST_ARRAYS.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22 },
         SMALLER_FRAGMENT_SIZE);
      /* Twice the max fragment size +1 */
      TEST_ARRAYS.put(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23 },
         SMALLER_FRAGMENT_SIZE);
   }
}
