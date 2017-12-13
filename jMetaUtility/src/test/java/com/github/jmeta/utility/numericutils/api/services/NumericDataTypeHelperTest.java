/**
 *
 * {@link NumericDataTypeHelperTests}.java
 *
 * @author Jens Ebert
 *
 * @date 02.01.2012
 */
package com.github.jmeta.utility.numericutils.api.services;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link NumericDataTypeHelperTest} tests the {@link NumericDataTypeUtil} class.
 */
public class NumericDataTypeHelperTest {

   private static final long MAX_UINT_PLUS_ONE = (Long.valueOf(Integer.MAX_VALUE) + 1) * 2;

   /**
    * Tests {@link NumericDataTypeUtil#unsignedValue(byte)}.
    */
   @Test
   public void test_unsignedValue_byte() {
      byte[] inputData = new byte[] { 0, 1, -1, -128, -2, -4, 50, -55 };
      int[] expectedData = new int[] { 0, 1, 255, 128, 254, 252, 50, 201 };

      if (inputData.length != expectedData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.unsignedValue(inputData[i]));
   }

   /**
    * Tests {@link NumericDataTypeUtil#unsignedValue(short)}.
    */
   @Test
   public void test_unsignedValue_short() {
      short[] inputData = new short[] { 0, 1, -1, -128, -2, -4, 50, -55, -32768, 32767 };
      int[] expectedData = new int[] { 0, 1, 65535, 65408, 65534, 65532, 50, 65481, 32768, 32767 };

      if (inputData.length != expectedData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.unsignedValue(inputData[i]));
   }

   /**
    * Tests {@link NumericDataTypeUtil#unsignedValue(int)}.
    */
   @Test
   public void test_unsignedValue_int() {
      int[] inputData = new int[] { 0, 1, -1, -2, -4, 50, -55, -32768, 32767, -128 };
      long[] expectedData = new long[] { 0, 1, MAX_UINT_PLUS_ONE - 1, MAX_UINT_PLUS_ONE - 2, MAX_UINT_PLUS_ONE - 4, 50,
         MAX_UINT_PLUS_ONE - 55, MAX_UINT_PLUS_ONE - 32768, 32767, MAX_UINT_PLUS_ONE - 128 };

      if (inputData.length != expectedData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.unsignedValue(inputData[i]));
   }

   /**
    * Tests {@link NumericDataTypeUtil#signedByteValue(long)}.
    */
   @Test
   public void test_signedValue_byte() {
      long[] inputData = new long[] { 0, 1, 255, 128, 254, 252, 50, 201 };
      byte[] expectedData = new byte[] { 0, 1, -1, -128, -2, -4, 50, -55 };

      if (expectedData.length != inputData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.signedByteValue(inputData[i]));
   }

   /**
    * Tests {@link NumericDataTypeUtil#signedShortValue(long)}.
    */
   @Test
   public void test_signedValue_short() {
      long[] inputData = new long[] { 0, 1, 65535, 65408, 65534, 65532, 50, 65481, 32768, 32767 };
      short[] expectedData = new short[] { 0, 1, -1, -128, -2, -4, 50, -55, -32768, 32767 };

      if (expectedData.length != inputData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.signedShortValue(inputData[i]));
   }

   /**
    * Tests {@link NumericDataTypeUtil#signedIntValue(long)}.
    */
   @Test
   public void test_signedValue_int() {
      long[] inputData = new long[] { 0, 1, MAX_UINT_PLUS_ONE - 1, MAX_UINT_PLUS_ONE - 2, MAX_UINT_PLUS_ONE - 4, 50,
         MAX_UINT_PLUS_ONE - 55, MAX_UINT_PLUS_ONE - 32768, 32767, MAX_UINT_PLUS_ONE - 128 };
      int[] expectedData = new int[] { 0, 1, -1, -2, -4, 50, -55, -32768, 32767, -128 };

      if (inputData.length != expectedData.length)
         throw new InvalidTestDataException("Length of input data must match length of expected output data", null);

      for (int i = 0; i < inputData.length; i++)
         Assert.assertEquals(expectedData[i], NumericDataTypeUtil.signedIntValue(inputData[i]));
   }
}
