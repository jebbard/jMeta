package com.github.jmeta.utility.numericutils.api.services;

/**
 * {@link NumericDataTypeUtil} provides helper functionality for the built-in numeric primitive types byte, short, int
 * and long.
 */
public class NumericDataTypeUtil {

   /**
    * Returns the unsigned representation of the byte value according to two's complement encoding. Because Java does
    * include an unsigned byte type, positive values bigger than 2<sup>7</sup>-1 need to be represented by next wider
    * data type's positive values.
    *
    * The highest one bit is simply NOT interpreted as a "sign bit" but all bits contribute to the positive value. Of
    * course, for positive numbers this method has no effect other than converting the value to an int.
    *
    * @param n
    *           The byte value to represent as an unsigned number.
    * @return The unsigned representation of the byte value as an int.
    */
   public static int unsignedValue(byte n) {
      return n & 0x00000FF;
   }

   /**
    * Returns the unsigned representation of the short value according to two's complement encoding. Because Java does
    * include an unsigned short type, positive values bigger than 2<sup>15</sup>-1 need to be represented by next wider
    * data type's positive values.
    *
    * The highest one bit is simply NOT interpreted as a "sign bit" but all bits contribute to the positive value. Of
    * course, for positive numbers this method has no effect other than converting the value to an int.
    *
    * @param n
    *           The short value to represent as an unsigned number.
    * @return The unsigned representation of the short value as an int.
    */
   public static int unsignedValue(short n) {
      return n & 0x0000FFFF;
   }

   /**
    * Returns the unsigned representation of the int value according to two's complement encoding. Because Java does
    * include an unsigned int type, positive values bigger than 2<sup>31</sup>-1 need to be represented by next wider
    * data type's positive values.
    *
    * The highest one bit is simply NOT interpreted as a "sign bit" but all bits contribute to the positive value. Of
    * course, for positive numbers this method has no effect other than converting the value to an long.
    *
    * @param n
    *           The int value to represent as an unsigned number.
    * @return The unsigned representation of the int value as a long.
    */
   public static long unsignedValue(int n) {
      /*
       * Implementation note: -------------------- The "&" bitwise and cannot be used here as this requires integers as
       * operands. The code "n & 0x0000000FFFFFFFF" would compile, but yield an at first unexpected result, as both
       * operands are implicitly casted to integer before doing the bitwise and, which results again in the value of n.
       */
      return (n < 0 ? MAX_POSITIVE_SIGNED_INT_PLUS_ONE + n : n);
   }

   /**
    * Returns the signed byte value for the given parameter
    * 
    * @param n
    *           An unsigned long
    * @return the signed byte value for the given parameter
    */
   public static byte signedByteValue(long n) {
      return Long.valueOf(n).byteValue();
   }

   /**
    * Returns the signed short value for the given parameter
    * 
    * @param n
    *           An unsigned long
    * @return the signed short value for the given parameter
    */
   public static short signedShortValue(long n) {
      return Long.valueOf(n).shortValue();
   }

   /**
    * Returns the signed int value for the given parameter
    * 
    * @param n
    *           An unsigned long
    * @return the signed int value for the given parameter
    */
   public static int signedIntValue(long n) {
      return Long.valueOf(n).intValue();
   }

   /**
    * Creates a new {@link NumericDataTypeUtil}. Private constructor because this is a static helper class only.
    */
   private NumericDataTypeUtil() {

   }

   private static long MAX_POSITIVE_SIGNED_INT_PLUS_ONE = 1 + (((long) Integer.MAX_VALUE) << 1) + 1;
}
