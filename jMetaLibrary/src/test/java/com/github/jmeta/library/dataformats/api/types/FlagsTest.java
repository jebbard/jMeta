/**
 *
 * {@link FlagsXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link FlagsTest} tests the {@link Flags} class.
 */
public abstract class FlagsTest {

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      FlagSpecification specLittleEndian;
      FlagSpecification specBigEndian;

      try {
         specLittleEndian = getLittleEndianFlagSpecification();
         specBigEndian = getBigEndianFlagSpecification();

         m_flagsLittleEndian = new Flags(specLittleEndian);
         m_flagsBigEndian = new Flags(specBigEndian);

         checkTestData();
      }

      catch (PreconditionUnfullfilledException e) {
         throw new InvalidTestDataException("Could not initialize flag specification.", e);
      }
   }

   /**
    * Tests {@link Flags#setFlag(String, boolean)} and {@link Flags#getFlag(String)}.
    */
   @Test
   public void test_setFlag_getFlag() {
      testSetFlagGetFlag(m_flagsBigEndian);
      testSetFlagGetFlag(m_flagsLittleEndian);
   }

   /**
    * Tests {@link Flags#asByte()}, {@link Flags#asShort()}, {@link Flags#asInt()}, {@link Flags#asLong()} for returning
    * zero if all flags are not set.
    */
   @Test
   public void test_asByte_asShort_asInteger_asLong_nothingSet() {
      testAsMethodsNoFlagsSet(m_flagsBigEndian);
      testAsMethodsNoFlagsSet(m_flagsLittleEndian);
   }

   /**
    * Tests {@link Flags#asShort}, {@link Flags#asInt}, {@link Flags#asLong} for throwing an
    * {@link PreconditionUnfullfilledException} correctly.
    */
   @Test
   public void test_asShort_asInteger_asLong_LengthException() {
      testAsMethodsLength(m_flagsBigEndian);
      testAsMethodsLength(m_flagsLittleEndian);
   }

   /**
    * Tests {@link Flags#fromShort}, {@link Flags#fromInt}, {@link Flags#fromLong} if setting all flags numerically
    * works correctly.
    */
   @Test
   public void test_fromShort_fromInteger_fromLong_allSet() {
      testFromMethodsAllFlagsSet(m_flagsBigEndian);
      testFromMethodsAllFlagsSet(m_flagsLittleEndian);
   }

   /**
    * Tests {@link Flags#fromShort}, {@link Flags#fromInt}, {@link Flags#fromLong} for throwing an
    * {@link PreconditionUnfullfilledException} correctly.
    */
   @Test
   public void test_fromShort_fromInteger_fromLong_LengthException() {
      testFromMethodsLength(m_flagsBigEndian);
      testFromMethodsLength(m_flagsLittleEndian);
   }

   /**
    * Tests all as and from methods for retrieving the same value that has been set.
    */
   @Test
   public void test_as_from_consistent() {
      testAsFromConsistent(m_flagsBigEndian);
      testAsFromConsistent(m_flagsLittleEndian);
   }

   /**
    * Returns a {@link FlagSpecification} with little endian byte order.
    *
    * @return A {@link FlagSpecification} with little endian byte order.
    */
   protected abstract FlagSpecification getLittleEndianFlagSpecification();

   /**
    * Returns a {@link FlagSpecification} with big endian byte order.
    *
    * @return A {@link FlagSpecification} with big endian byte order.
    */
   protected abstract FlagSpecification getBigEndianFlagSpecification();

   /**
    * Tests {@link Flags#asByte()}, {@link Flags#asShort()}, {@link Flags#asInt()}, {@link Flags#asLong()} for returning
    * zero if all flags are not set.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testAsMethodsNoFlagsSet(Flags flags) {
      FlagSpecification spec = flags.getSpecification();

      // Unset all flags
      for (String flagName : spec.getFlagDescriptions().keySet())
         flags.setFlag(flagName, false);

      // The numeric representation must be 0.
      Assert.assertEquals(0, flags.asByte());

      if (spec.getByteLength() >= Flags.SHORT_BYTE_LENGTH)
         Assert.assertEquals(0, flags.asShort());
      if (spec.getByteLength() >= Flags.INT_BYTE_LENGTH)
         Assert.assertEquals(0, flags.asInt());
      if (spec.getByteLength() >= Flags.LONG_BYTE_LENGTH)
         Assert.assertEquals(0, flags.asLong());

      for (int i = 0; i < spec.getByteLength(); ++i)
         Assert.assertEquals(0, flags.asArray()[i]);
   }

   /**
    * Tests {@link Flags#fromShort}, {@link Flags#fromInt}, {@link Flags#fromLong} if setting all flags numerically
    * works correctly.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testFromMethodsAllFlagsSet(Flags flags) {
      FlagSpecification spec = flags.getSpecification();

      // Numeric values that have all 1s bitwise
      final byte byteValue = (byte) 0xFF;
      final short shortValue = (short) 0xFFFF;
      final int intValue = 0xFFFFFFFF;
      final long longValue = 0xFFFFFFFFFFFFFFFFL;

      ByteBuffer bb = ByteBuffer.allocate(spec.getByteLength());

      for (int i = 0; i < spec.getByteLength(); ++i)
         bb.put(byteValue);

      byte[] byteArrayValue = bb.array();

      if (spec.getByteLength() == Flags.LONG_BYTE_LENGTH)
         flags.fromLong(longValue);

      else if (spec.getByteLength() == Flags.INT_BYTE_LENGTH)
         flags.fromInt(intValue);

      else if (spec.getByteLength() == Flags.SHORT_BYTE_LENGTH)
         flags.fromShort(shortValue);

      else if (spec.getByteLength() == 1)
         flags.fromByte(byteValue);

      else
         flags.fromArray(byteArrayValue);

      for (String flagName : spec.getFlagDescriptions().keySet())
         Assert.assertEquals(true, flags.getFlag(flagName));
   }

   /**
    * Tests {@link Flags#setFlag(String, boolean)} and {@link Flags#getFlag(String)}.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testSetFlagGetFlag(Flags flags) {
      for (String flagName : flags.getSpecification().getFlagDescriptions().keySet()) {
         flags.setFlag(flagName, false);
         Assert.assertEquals(false, flags.getFlag(flagName));
         Assert.assertEquals(0, flags.getFlagIntegerValue(flagName));
         flags.setFlag(flagName, true);
         Assert.assertEquals(true, flags.getFlag(flagName));
         Assert.assertEquals(1, flags.getFlagIntegerValue(flagName));
      }
   }

   /**
    * Tests {@link Flags#asShort}, {@link Flags#asInt}, {@link Flags#asLong} for throwing an
    * {@link PreconditionUnfullfilledException} correctly.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testAsMethodsLength(Flags flags) {
      FlagSpecification spec = flags.getSpecification();

      if (spec.getByteLength() < Flags.SHORT_BYTE_LENGTH) {
         try {
            flags.asShort();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.asInt();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.asLong();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }

      else if (spec.getByteLength() < Flags.INT_BYTE_LENGTH) {
         try {
            flags.asShort();
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.asInt();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.asLong();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }

      else if (spec.getByteLength() < Flags.LONG_BYTE_LENGTH) {
         try {
            flags.asShort();
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.asInt();
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.asLong();
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }

   /**
    * Tests {@link Flags#fromShort}, {@link Flags#fromInt}, {@link Flags#fromLong} for throwing an
    * {@link PreconditionUnfullfilledException} correctly.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testFromMethodsLength(Flags flags) {
      final short shortValue = (short) 456;
      final int intValue = 43598;
      final long longValue = 27546382345454L;

      FlagSpecification spec = flags.getSpecification();

      byte[] arrayToTest = new byte[spec.getByteLength() + 2];

      try {
         flags.fromArray(arrayToTest);
         Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
      }

      catch (PreconditionUnfullfilledException e1) {
         // Nothing to do here
         Assert.assertNotNull("Exception as expected", e1);
      }

      if (spec.getByteLength() < Flags.SHORT_BYTE_LENGTH) {
         try {
            flags.fromShort(shortValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.fromInt(intValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.fromLong(longValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }

      else if (spec.getByteLength() < Flags.INT_BYTE_LENGTH) {
         try {
            flags.fromShort(shortValue);
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.fromInt(intValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }

         try {
            flags.fromLong(longValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }

      else if (spec.getByteLength() < Flags.LONG_BYTE_LENGTH) {
         try {
            flags.fromShort(shortValue);
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.fromInt(intValue);
         }

         catch (PreconditionUnfullfilledException e) {
            Assert.fail("Unexpected exception: " + e);
         }

         try {
            flags.fromLong(longValue);
            Assert.fail("Expected exception: " + PreconditionUnfullfilledException.class);
         }

         catch (PreconditionUnfullfilledException e) {
            // Nothing to do here
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }

   /**
    * Tests all as and from methods for retrieving the same value that has been set.
    *
    * @param flags
    *           The {@link Flags} to test.
    */
   public void testAsFromConsistent(Flags flags) {
      FlagSpecification spec = flags.getSpecification();

      final byte byteValue = (byte) -124;
      final short shortValue = (short) 456;
      final int intValue = 43598;
      final long longValue = 275463823434563L;

      byte[] arrayToTest = new byte[spec.getByteLength()];

      for (int i = 0; i < spec.getByteLength(); ++i)
         arrayToTest[i] = 0x6A;

      try {
         flags.fromArray(arrayToTest);
         Assert.assertArrayEquals(arrayToTest, flags.asArray());

         flags.fromByte(byteValue);
         Assert.assertEquals(byteValue, flags.asByte());

         if (spec.getByteLength() >= Flags.SHORT_BYTE_LENGTH) {
            flags.fromShort(shortValue);
            Assert.assertEquals(shortValue, flags.asShort());
         }

         if (spec.getByteLength() >= Flags.INT_BYTE_LENGTH) {
            flags.fromInt(intValue);
            Assert.assertEquals(intValue, flags.asInt());
         }

         if (spec.getByteLength() >= Flags.LONG_BYTE_LENGTH) {
            flags.fromLong(longValue);
            Assert.assertEquals(longValue, flags.asLong());
         }
      }

      catch (PreconditionUnfullfilledException e) {
         Assert.fail("Unexpected exception: " + e);
      }
   }

   /**
    * Checks the test data to be valid.
    *
    * @throws PreconditionUnfullfilledException
    *            If data is not valid with a speaking message what is wrong in detail.
    */
   private void checkTestData() {
      if (m_flagsBigEndian.getSpecification().getByteOrdering() != ByteOrder.BIG_ENDIAN)
         throw new InvalidTestDataException("Byte ordering for the big endian spec must be ByteOrder.BIG_ENDIAN", null);

      if (m_flagsLittleEndian.getSpecification().getByteOrdering() != ByteOrder.LITTLE_ENDIAN)
         throw new InvalidTestDataException("Byte ordering for the little endian spec must be ByteOrder.LITTLE_ENDIAN", null);
   }

   private Flags m_flagsLittleEndian;
   private Flags m_flagsBigEndian;
}
