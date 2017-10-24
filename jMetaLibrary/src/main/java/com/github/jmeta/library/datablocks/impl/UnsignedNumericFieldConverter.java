/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.numericutils.api.services.NumericDataTypeUtil;

/**
 * {@link UnsignedNumericFieldConverter}
 *
 */
public class UnsignedNumericFieldConverter implements FieldConverter<Long> {

   private static final int MAX_LONG_BYTE_SIZE = Long.SIZE / Byte.SIZE;

   @Override
   public Long toInterpreted(BinaryValue binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");

      long fieldByteCount = binaryValue.getTotalSize();

      if (fieldByteCount > MAX_LONG_BYTE_SIZE)
         throw new BinaryValueConversionException(
            "Numeric fields may not be longer than " + MAX_LONG_BYTE_SIZE
               + " bytes.",
            null, desc, binaryValue, byteOrder, characterEncoding);

      ByteBuffer buffer = ByteBuffer.wrap(binaryValue.getFragment(0));

      buffer.order(byteOrder);

      if (fieldByteCount == 1)
         return (long) NumericDataTypeUtil.unsignedValue(buffer.get());

      else if (fieldByteCount == 2)
         return (long) NumericDataTypeUtil.unsignedValue(buffer.getShort());

      else if (fieldByteCount <= 4)
         return (long) NumericDataTypeUtil.unsignedValue(buffer.getInt());

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE) {
         final long longValue = buffer.getLong();

         if (longValue < 0)
            throw new BinaryValueConversionException(
               "Negative long values currently cannot be represented as unsigned. Value: "
                  + longValue + ".",
               null, desc, binaryValue, byteOrder, characterEncoding);

         return longValue;
      }

      return null;
   }

   @Override
   public BinaryValue toBinary(Long interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      long fieldByteCount = desc.getMaximumByteLength();

      if (fieldByteCount > MAX_LONG_BYTE_SIZE)
         throw new InterpretedValueConversionException(
            "Numeric fields may not be longer than " + MAX_LONG_BYTE_SIZE
               + " bytes.",
            null, desc, interpretedValue, byteOrder, characterEncoding);

      ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) fieldByteCount]);

      if (fieldByteCount == 1)
         buffer.put(interpretedValue.byteValue());

      else if (fieldByteCount == 2)
         buffer.putShort(interpretedValue.shortValue());

      else if (fieldByteCount <= 4)
         buffer.putInt(interpretedValue.intValue());

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE)
         buffer.putLong(interpretedValue);

      return new BinaryValue(buffer);
   }

}
