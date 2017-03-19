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
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.util.javautil.common.err.Reject;

// TODO stage2_006: For enumerated fields, ensure during load time that there is
// a constructor of the interpreted value type that accepts single string argument.
/**
 * {@link EnumeratedFieldConverter}
 *
 * @param <T>
 */
public class EnumeratedFieldConverter<T> implements IFieldConverter<T> {

   @SuppressWarnings("unchecked")
   @Override
   public T toInterpreted(BinaryValue binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");

      if (binaryValue.getTotalSize() > Integer.MAX_VALUE)
         throw new BinaryValueConversionException(
            "Enumerated fields may not be longer than " + Integer.MAX_VALUE
               + " bytes.",
            null, desc, binaryValue, byteOrder, characterEncoding);

      // CONFIG_CHECK: byte values d�rfen auch nur einmal in der Map vorkommen
      Map<T, byte[]> enumValues = (Map<T, byte[]>) desc.getFieldProperties()
         .getEnumeratedValues();

      for (Iterator<T> enumValuesIterator = enumValues.keySet()
         .iterator(); enumValuesIterator.hasNext();) {
         T byteMapping = enumValuesIterator.next();

         if (Arrays.equals(enumValues.get(byteMapping),
            binaryValue.getFragment(0)))
            return byteMapping;
      }

      return (T) desc.getFieldProperties().getDefaultValue();
   }

   @Override
   public BinaryValue toBinary(T interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      @SuppressWarnings("unchecked")
      Map<T, byte[]> enumValues = (Map<T, byte[]>) desc.getFieldProperties()
         .getEnumeratedValues();

      if (!enumValues.containsKey(interpretedValue))
         throw new InterpretedValueConversionException(
            "Enumerated fields may not be longer than " + Integer.MAX_VALUE
               + " bytes.",
            null, desc, interpretedValue, byteOrder, characterEncoding);

      return new BinaryValue(enumValues.get(interpretedValue));
   }

}
