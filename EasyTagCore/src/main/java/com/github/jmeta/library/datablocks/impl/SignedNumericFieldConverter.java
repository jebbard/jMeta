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

import com.github.jmeta.library.datablocks.api.exception.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exception.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.type.BinaryValue;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.utility.dbc.api.services.Reject;

import de.je.util.javautil.common.num.NumericDataTypeHelper;

/**
 * {@link SignedNumericFieldConverter}
 *
 */
public class SignedNumericFieldConverter implements IFieldConverter<Long> {

   private static final int MAX_LONG_BYTE_SIZE = 8;

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
         return (long) buffer.get();

      else if (fieldByteCount == 2)
         return (long) buffer.getShort();

      else if (fieldByteCount <= 4)
         return (long) buffer.getInt();

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE)
         return buffer.getLong();

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
         buffer.put(NumericDataTypeHelper.signedByteValue(interpretedValue));

      else if (fieldByteCount == 2)
         buffer
            .putShort(NumericDataTypeHelper.signedShortValue(interpretedValue));

      else if (fieldByteCount <= 4)
         buffer.putInt(NumericDataTypeHelper.signedIntValue(interpretedValue));

      else if (fieldByteCount <= MAX_LONG_BYTE_SIZE)
         buffer.putLong(interpretedValue);

      return new BinaryValue(buffer);
   }

}
