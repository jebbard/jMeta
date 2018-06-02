/**
 *
 * {@link ByteOrders}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2018
 *
 */
package com.github.jmeta.utility.byteutils.api.services;

import java.nio.ByteOrder;

/**
 * {@link ByteOrders} provides utility functions for {@link ByteOrder} handling.
 */
public class ByteOrders {

   /**
    * Converts a string representation of a {@link ByteOrder} to an instance of {@link ByteOrder}. Throws a
    * {@link RuntimeException} if this string representation is unsupported.
    * 
    * @param byteOrder
    *           The byte order string representation to convert
    * @return The {@link ByteOrder} instance corresponding to this string representation.
    */
   public static ByteOrder fromString(String byteOrder) {
      if (byteOrder.equals(ByteOrder.BIG_ENDIAN.toString())) {
         return ByteOrder.BIG_ENDIAN;
      }
      if (byteOrder.equals(ByteOrder.BIG_ENDIAN.toString())) {
         return ByteOrder.LITTLE_ENDIAN;
      }

      throw new RuntimeException("Invalid byte order string: <" + byteOrder + ">");
   }
}
