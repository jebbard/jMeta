/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.datablocks.impl.SignedNumericFieldConverter;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link Lyrics3v2StringSizeIntegerConverter}
 *
 */
public class Lyrics3v2StringSizeIntegerConverter extends SignedNumericFieldConverter {

   private static final int DECIMAL_RADIX = 10;

   private static final int MAX_LYRICS3v2_SIZE_FIELD_LENGTH = 6;

   private static final int MAX_SIZE = DECIMAL_RADIX * DECIMAL_RADIX * DECIMAL_RADIX * DECIMAL_RADIX * DECIMAL_RADIX
      * DECIMAL_RADIX - 1;

   // TODO primeRefactor005: Finalize and document this method
   @Override
   public Long toInterpreted(BinaryValue binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");

      if (binaryValue.getTotalSize() > MAX_LYRICS3v2_SIZE_FIELD_LENGTH)
         throw new BinaryValueConversionException(
            "Total size of binary value containing Lyrics3v2 size information must not be bigger than 6 bytes", null,
            desc, binaryValue, byteOrder, characterEncoding);

      byte[] lengthFieldBytes = binaryValue.getBytes(0, (int) binaryValue.getTotalSize());

      int totalSize = 0;
      int digitMultiplier = 1;

      for (int i = lengthFieldBytes.length - 1; i >= 0; --i) {
         int nextDigit = Character.digit(lengthFieldBytes[i], DECIMAL_RADIX);

         if (nextDigit == -1)
            throw new BinaryValueConversionException("Size field's value <" + binaryValue + "> may contain digits only",
               null, desc, binaryValue, byteOrder, characterEncoding);

         totalSize += nextDigit * digitMultiplier;

         digitMultiplier *= DECIMAL_RADIX;
      }

      return Long.valueOf(totalSize);
   }

   @Override
   public BinaryValue toBinary(Long interpretedValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      if (interpretedValue > MAX_SIZE)
         throw new InterpretedValueConversionException("Lyrics3v2 size fields may not be larger than " + MAX_SIZE, null,
            desc, interpretedValue, byteOrder, characterEncoding);

      // TODO primeRefactor004: implement

      return super.toBinary(interpretedValue, desc, byteOrder, characterEncoding);
   }

}
