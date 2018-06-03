/**
 *
 * {@link AbstractBaseFieldConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 02.06.2018
 *
 */
package com.github.jmeta.library.dataformats.api.types.converter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractBaseFieldConverter}
 *
 */
public abstract class AbstractBaseFieldConverter<T> implements FieldConverter<T> {

   private T findEnumeratedInterpretedValue(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      Map<T, byte[]> enumValues = (Map<T, byte[]>) desc.getFieldProperties().getEnumeratedValues();

      if (!enumValues.isEmpty()) {
         if (binaryValue.remaining() > Integer.MAX_VALUE)
            throw new BinaryValueConversionException(
               "Enumerated fields may not be longer than " + Integer.MAX_VALUE + " bytes.", null, desc, binaryValue,
               byteOrder, characterEncoding);

         byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(binaryValue);

         for (Iterator<T> enumValuesIterator = enumValues.keySet().iterator(); enumValuesIterator.hasNext();) {
            T byteMapping = enumValuesIterator.next();

            if (Arrays.equals(enumValues.get(byteMapping), copiedBytes))
               return byteMapping;
         }
      }

      return null;
   }

   private ByteBuffer findEnumeratedBinaryValue(T interpretedValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws InterpretedValueConversionException {

      Map<T, byte[]> enumValues = (Map<T, byte[]>) desc.getFieldProperties().getEnumeratedValues();

      if (!enumValues.isEmpty()) {
         if (enumValues.containsKey(interpretedValue)) {
            return ByteBuffer.wrap(enumValues.get(interpretedValue));
         }
      }

      return null;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.converter.FieldConverter#toInterpreted(java.nio.ByteBuffer,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   public T toInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "byteValue");

      T enumeratedInterpretedValue = findEnumeratedInterpretedValue(binaryValue, desc, byteOrder, characterEncoding);
      if (enumeratedInterpretedValue != null) {
         return enumeratedInterpretedValue;
      }

      return convertBinaryToInterpreted(binaryValue, desc, byteOrder, characterEncoding);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.converter.FieldConverter#toBinary(java.lang.Object,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   public ByteBuffer toBinary(T interpretedValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      ByteBuffer enumeratedBinaryValue = findEnumeratedBinaryValue(interpretedValue, desc, byteOrder,
         characterEncoding);
      if (enumeratedBinaryValue != null) {
         return enumeratedBinaryValue;
      }

      return convertInterpretedToBinary(interpretedValue, desc, byteOrder, characterEncoding);
   }

   /**
    * @param binaryValue
    * @param desc
    * @param byteOrder
    * @param characterEncoding
    * @return
    */
   protected abstract T convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) throws BinaryValueConversionException;

   /**
    * @param interpretedValue
    * @param desc
    * @param byteOrder
    * @param characterEncoding
    * @return
    */
   protected abstract ByteBuffer convertInterpretedToBinary(T interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) throws InterpretedValueConversionException;

}
