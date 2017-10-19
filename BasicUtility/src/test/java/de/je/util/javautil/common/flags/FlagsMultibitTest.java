/**
 *
 * {@link FlagsMultibitTest}.java
 *
 * @author jebert
 *
 * @date 02.10.2011
 */
package de.je.util.javautil.common.flags;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link FlagsMultibitTest}
 *
 */
public class FlagsMultibitTest extends FlagsTest {

   /**
    * @see de.je.util.javautil.common.flags.FlagsTest#getLittleEndianFlagSpecification()
    */
   @Override
   protected FlagSpecification getLittleEndianFlagSpecification() {
      return new FlagSpecification(FLAG_DESCRIPTIONS, BYTE_LENGTH, ByteOrder.LITTLE_ENDIAN, new byte[BYTE_LENGTH]);
   }

   /**
    * @see de.je.util.javautil.common.flags.FlagsTest#getBigEndianFlagSpecification()
    */
   @Override
   protected FlagSpecification getBigEndianFlagSpecification() {
      return new FlagSpecification(FLAG_DESCRIPTIONS, BYTE_LENGTH, ByteOrder.BIG_ENDIAN, new byte[BYTE_LENGTH]);
   }

   /**
    * Tests {@link Flags#getFlagIntegerValue(String)}.
    */
   @Test
   public void test_getFlagIntValue() {
      Flags flagsLE = new Flags(getLittleEndianFlagSpecification());

      checkGetFlagIntValue(flagsLE, EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN);

      Flags flagsBE = new Flags(getBigEndianFlagSpecification());

      checkGetFlagIntValue(flagsBE, EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN);
   }

   /**
    * Checks the method {@link Flags#getFlagIntegerValue(String)}.
    *
    * @param flags
    *           The flags to test.
    * @param expectedValuesMap
    *           The map of expected values.
    */
   private void checkGetFlagIntValue(Flags flags, Map<Integer, List<Integer>> expectedValuesMap) {
      for (Iterator<Integer> iterator = expectedValuesMap.keySet().iterator(); iterator.hasNext();) {
         int intValueOfWholeFlags = iterator.next();
         List<Integer> expectedFlagValues = expectedValuesMap.get(intValueOfWholeFlags);

         flags.fromInt(intValueOfWholeFlags);

         for (int i = 0; i < expectedFlagValues.size(); ++i) {
            int expectedFlagIntValue = expectedFlagValues.get(i);

            int actualFlagIntValue = flags.getFlagIntegerValue(FLAG_DESCRIPTIONS.get(i).getFlagName());

            Assert.assertEquals(expectedFlagIntValue, actualFlagIntValue);
         }

         Assert.assertEquals(intValueOfWholeFlags, flags.asInt());
      }
   }

   private static final int BYTE_LENGTH = 4;

   private static List<FlagDescription> FLAG_DESCRIPTIONS = new ArrayList<>();

   static {
      FLAG_DESCRIPTIONS.add(new FlagDescription("SingleBitty1", new BitAddress(0, 0), "", 1, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("MultiBitty1", new BitAddress(0, 1), "", 2, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("MultiBitty2", new BitAddress(0, 3), "", 3, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("SingleBitty2", new BitAddress(0, 6), "", 1, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("MultiBitty3", new BitAddress(0, 7), "", 5, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("MultiBitty4", new BitAddress(1, 4), "", 14, null));
      FLAG_DESCRIPTIONS.add(new FlagDescription("SingleBitty3", new BitAddress(3, 2), "", 1, null));
   }

   private static Map<Integer, List<Integer>> EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN = new HashMap<>();

   static {
      List<Integer> expectedFlagValues0 = new ArrayList<>();

      for (int i = 0; i < FLAG_DESCRIPTIONS.size(); ++i)
         expectedFlagValues0.add(0);

      EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN.put(0, expectedFlagValues0);

      List<Integer> expectedFlagValues4456789 = new ArrayList<>();

      /*
       * Binary representation of 4456789: 00000000010001000000000101010101 || | || | | 76 5 43 2 1
       * System.out.println(Integer.toBinaryString(4456789));
       */

      expectedFlagValues4456789.add(1);
      expectedFlagValues4456789.add(2);
      expectedFlagValues4456789.add(2);
      expectedFlagValues4456789.add(1);
      expectedFlagValues4456789.add(2);
      expectedFlagValues4456789.add(1088);
      expectedFlagValues4456789.add(0);

      EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN.put(4456789, expectedFlagValues4456789);

      List<Integer> expectedFlagValues1178869702 = new ArrayList<>();

      /*
       * Binary representation of 1178869702: 01000110010001000001111111000110 || | || | | 76 5 43 2 1
       * System.out.println(Integer.toBinaryString(1178869702));
       */

      expectedFlagValues1178869702.add(0);
      expectedFlagValues1178869702.add(3);
      expectedFlagValues1178869702.add(0);
      expectedFlagValues1178869702.add(1);
      expectedFlagValues1178869702.add(31);
      expectedFlagValues1178869702.add(1 + 64 + 1024 + 8192);
      expectedFlagValues1178869702.add(1);

      EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN.put(1178869702, expectedFlagValues1178869702);

      List<Integer> expectedFlagValuesMinus1 = new ArrayList<>();

      /*
       * Binary representation of -1: 11111111111111111111111111111111 || | || | | 76 5 43 2 1
       * System.out.println(Integer.toBinaryString(-1));
       */

      expectedFlagValuesMinus1.add(1);
      expectedFlagValuesMinus1.add(3);
      expectedFlagValuesMinus1.add(7);
      expectedFlagValuesMinus1.add(1);
      expectedFlagValuesMinus1.add(31);
      expectedFlagValuesMinus1.add(16383);
      expectedFlagValuesMinus1.add(1);

      EXPECTED_FLAG_INT_VALUES_LITTLE_ENDIAN.put(-1, expectedFlagValuesMinus1);
   }

   private static Map<Integer, List<Integer>> EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN = new HashMap<>();

   static {
      // TODO test with big endian
      // List<Integer> expectedFlagValues0 = new ArrayList<Integer>();
      //
      // for (int i = 0; i < FLAG_DESCRIPTIONS.size(); ++i)
      // expectedFlagValues0.add(0);
      //
      // EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN.put(0, expectedFlagValues0);
      //
      // List<Integer> expectedFlagValues4456789 = new ArrayList<Integer>();
      //
      // /* Binary representation of 4456789:
      // 00000000010001000000000101010101
      // || | || | |
      // 76 5 43 2 1
      //
      // System.out.println(Integer.toBinaryString(4456789));
      // */
      //
      // expectedFlagValues4456789.add(1);
      // expectedFlagValues4456789.add(2);
      // expectedFlagValues4456789.add(2);
      // expectedFlagValues4456789.add(1);
      // expectedFlagValues4456789.add(2);
      // expectedFlagValues4456789.add(1088);
      // expectedFlagValues4456789.add(0);
      //
      // EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN.put(4456789, expectedFlagValues4456789);
      //
      // List<Integer> expectedFlagValues1178869702 = new ArrayList<Integer>();
      //
      // /* Binary representation of 1178869702:
      // 01000110010001000001111111000110
      //
      // || | || | |
      // 76 5 43 2 1
      //
      // System.out.println(Integer.toBinaryString(1178869702));
      // */
      //
      // expectedFlagValues1178869702.add(0);
      // expectedFlagValues1178869702.add(3);
      // expectedFlagValues1178869702.add(0);
      // expectedFlagValues1178869702.add(1);
      // expectedFlagValues1178869702.add(31);
      // expectedFlagValues1178869702.add(1+64+1024+8192);
      // expectedFlagValues1178869702.add(1);
      //
      // EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN.put(1178869702, expectedFlagValues1178869702);
      //
      // List<Integer> expectedFlagValuesMinus1 = new ArrayList<Integer>();
      //
      // /* Binary representation of -1:
      // 11111111111111111111111111111111
      //
      // || | || | |
      // 76 5 43 2 1
      //
      // System.out.println(Integer.toBinaryString(-1));
      // */
      //
      // expectedFlagValuesMinus1.add(1);
      // expectedFlagValuesMinus1.add(3);
      // expectedFlagValuesMinus1.add(7);
      // expectedFlagValuesMinus1.add(1);
      // expectedFlagValuesMinus1.add(31);
      // expectedFlagValuesMinus1.add(16383);
      // expectedFlagValuesMinus1.add(1);
      //
      // EXPECTED_FLAG_INT_VALUES_BIG_ENDIAN.put(-1, expectedFlagValuesMinus1);
   }
}
