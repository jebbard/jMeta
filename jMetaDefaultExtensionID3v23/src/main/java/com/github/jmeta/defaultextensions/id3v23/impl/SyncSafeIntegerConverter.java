/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;

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
      System.out.println("Converting ID3v2.x bytes to sync safe integer");
      ByteBuffer bb = ByteBuffer.wrap(new byte[] { 0, 0, 0xf, 0x76 });
      bb.order(ByteOrder.BIG_ENDIAN);
      System.out.println(new SyncSafeIntegerConverter().synchSafeToInt(bb.getInt()));
      System.out.println(new SyncSafeIntegerConverter().intToSynchSafe(2038));

      int syncSafeSize = new SyncSafeIntegerConverter().intToSynchSafe(2052);

      System.out.println(new SyncSafeIntegerConverter().intToSynchSafe(syncSafeSize));

      ByteBuffer bbout = ByteBuffer.allocate(4);
      bbout.order(ByteOrder.BIG_ENDIAN);
      bbout.putInt(syncSafeSize);
      System.out.println(Arrays.toString(bbout.array()));

      ByteBuffer bbout2 = ByteBuffer.allocate(4);
      bbout2.order(ByteOrder.BIG_ENDIAN);
      bbout2.putInt(1876);
      System.out.println(Arrays.toString(bbout2.array()));

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
