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
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FlagsFieldConverter}
 *
 */
public class FlagsFieldConverter implements FieldConverter<Flags> {

   @Override
   public Flags toInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "spec");
      Reject.ifNull(binaryValue, "byteValue");

      int staticFlagLength = desc.getFieldProperties().getFlagSpecification().getByteLength();
      if (binaryValue.remaining() > staticFlagLength)
         throw new BinaryValueConversionException("Flags fields may not be longer than " + staticFlagLength + " bytes.",
            null, desc, binaryValue, byteOrder, characterEncoding);

      FlagSpecification flagSpec = desc.getFieldProperties().getFlagSpecification();

      final Flags flags = new Flags(flagSpec);

      byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(binaryValue);

      flags.fromArray(copiedBytes);

      return flags;
   }

   @Override
   public ByteBuffer toBinary(Flags interpretedValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      return ByteBuffer.wrap(interpretedValue.asArray());
   }

}
