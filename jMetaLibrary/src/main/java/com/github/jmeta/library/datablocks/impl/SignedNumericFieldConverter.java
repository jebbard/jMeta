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
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.numericutils.api.services.NumericDataTypeUtil;

/**
 * {@link SignedNumericFieldConverter}
 *
 */
public class SignedNumericFieldConverter extends AbstractBaseFieldConverter<Long> {

   private static final int MAX_LONG_BYTE_SIZE = 8;

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractBaseFieldConverter#convertBinaryToInterpreted(java.nio.ByteBuffer,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected Long convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      long fieldByteCount = binaryValue.remaining();

      if (fieldByteCount > MAX_LONG_BYTE_SIZE)
         throw new BinaryValueConversionException(
            "Numeric fields may not be longer than " + MAX_LONG_BYTE_SIZE + " bytes.", null, desc, binaryValue,
            byteOrder, characterEncoding);

      ByteBuffer copiedBuffer = binaryValue.asReadOnlyBuffer();

      copiedBuffer.order(byteOrder);

      if (fieldByteCount == 1)
         return (long) copiedBuffer.get();

      else if (fieldByteCount == 2)
         return (long) copiedBuffer.getShort();

      else if (fieldByteCount <= 4)
         return (long) copiedBuffer.getInt();

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE)
         return copiedBuffer.getLong();

      return null;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractBaseFieldConverter#convertInterpretedToBinary(java.lang.Object,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected ByteBuffer convertInterpretedToBinary(Long interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) throws InterpretedValueConversionException {

      long fieldByteCount = desc.getMaximumByteLength();

      if (fieldByteCount > MAX_LONG_BYTE_SIZE)
         throw new InterpretedValueConversionException(
            "Numeric fields may not be longer than " + MAX_LONG_BYTE_SIZE + " bytes.", null, desc, interpretedValue,
            byteOrder, characterEncoding);

      ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) fieldByteCount]);

      if (fieldByteCount == 1)
         buffer.put(NumericDataTypeUtil.signedByteValue(interpretedValue));

      else if (fieldByteCount == 2)
         buffer.putShort(NumericDataTypeUtil.signedShortValue(interpretedValue));

      else if (fieldByteCount <= 4)
         buffer.putInt(NumericDataTypeUtil.signedIntValue(interpretedValue));

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE)
         buffer.putLong(interpretedValue);

      return buffer;
   }
}
