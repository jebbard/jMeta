/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package de.je.jmeta.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.flags.FlagSpecification;
import de.je.util.javautil.common.flags.Flags;

/**
 * {@link FlagsFieldConverter}
 *
 */
public class FlagsFieldConverter implements IFieldConverter<Flags> {

   @Override
   public Flags toInterpreted(BinaryValue binaryValue,
      DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "spec");
      Reject.ifNull(binaryValue, "byteValue");

      int staticFlagLength = desc.getFieldProperties().getFlagSpecification()
         .getByteLength();
      if (binaryValue.getTotalSize() > staticFlagLength)
         throw new BinaryValueConversionException(
            "Flags fields may not be longer than " + staticFlagLength
               + " bytes.",
            null, desc, binaryValue, byteOrder, characterEncoding);

      FlagSpecification flagSpec = desc.getFieldProperties()
         .getFlagSpecification();

      final Flags flags = new Flags(flagSpec);

      flags.fromArray(binaryValue.getFragment(0));

      return flags;
   }

   @Override
   public BinaryValue toBinary(Flags interpretedValue,
      DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      return new BinaryValue(interpretedValue.asArray());
   }

}
