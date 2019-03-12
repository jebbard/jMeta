/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.converter.SignedNumericFieldConverter;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link SyncSafeIntegerConverter}
 *
 */
public class SyncSafeIntegerConverter extends SignedNumericFieldConverter {

   private static final int FROM_CONVERSION_MASK = 0x7F000000;

   private static final int TO_CONVERSION_MASK = 0x7F;

   private static final int MAX_SYNC_SAFE_INTEGER = 2 >> 28;

   @Override
   public Long toInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");
      if (binaryValue.remaining() != Integer.SIZE / Byte.SIZE) {
         throw new BinaryValueConversionException(
            "ID3v23 size fields must have integer (" + Integer.SIZE / Byte.SIZE + " bytes) size", null, desc,
            binaryValue, byteOrder, characterEncoding);
      }

      byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(binaryValue);

      int size = ByteBuffer.wrap(copiedBytes).getInt();

      return Long.valueOf(synchSafeToInt(size));
   }

   public static void main(String[] args) {
      System.out.println("SyncSafe integer converter");
      System.out.print("Enter normal integer: ");
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

      try {
         String intString = reader.readLine();

         int normalInt = Integer.parseInt(intString);
         int syncSafe = new SyncSafeIntegerConverter().intToSynchSafe(normalInt);

         System.out.println(
            "Sync safe version of " + normalInt + " is = " + syncSafe + "d = " + Integer.toHexString(syncSafe) + "h");
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Override
   public ByteBuffer toBinary(Long interpretedValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      if (interpretedValue > MAX_SYNC_SAFE_INTEGER) {
         throw new InterpretedValueConversionException(
            "ID3v23 sync-safe integers may not be larger than " + MAX_SYNC_SAFE_INTEGER, null, desc, interpretedValue,
            byteOrder, characterEncoding);
      }

      long converted = intToSynchSafe(interpretedValue.intValue());

      return super.toBinary(converted, desc, byteOrder, characterEncoding);
   }

   /**
    * Converts a usual integer to a synchsafe integer.
    *
    * @param usualInteger
    *           The usual integer to convert.
    * @return A synchsafe representation of the given integer.
    */
   private int intToSynchSafe(int usualInteger) {

      int synchSafeInteger = 0;
      int shiftedInteger = usualInteger;
      int mask = TO_CONVERSION_MASK;

      for (int i = 0; i < Integer.SIZE / Byte.SIZE; ++i) {
         synchSafeInteger += shiftedInteger & mask;
         shiftedInteger <<= 1;
         mask <<= Byte.SIZE;
      }

      return synchSafeInteger;
   }

   /**
    * Converts a synchsafe integer to a usual integer.
    *
    * @param synchSafeInteger
    *           The synchsafe integer to convert.
    * @return The usual integer.
    */
   private int synchSafeToInt(int synchSafeInteger) {

      int usualInteger = 0;
      int mask = FROM_CONVERSION_MASK;

      for (int i = 0; i < Integer.SIZE / Byte.SIZE; ++i) {
         usualInteger >>= 1;
         usualInteger |= synchSafeInteger & mask;
         mask >>= Byte.SIZE;
      }

      return usualInteger;
   }

}
