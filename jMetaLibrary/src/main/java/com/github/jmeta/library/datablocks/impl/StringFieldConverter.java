/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.library.datablocks.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;

/**
 * {@link StringFieldConverter}
 *
 */
public class StringFieldConverter extends AbstractBaseFieldConverter<String> {

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractBaseFieldConverter#convertBinaryToInterpreted(java.nio.ByteBuffer,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected String convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
      Charset characterEncoding) throws BinaryValueConversionException {

      if (binaryValue.remaining() > Integer.MAX_VALUE)
         throw new BinaryValueConversionException(
            "String fields may not be longer than " + Integer.MAX_VALUE + " bytes.", null, desc, binaryValue, byteOrder,
            characterEncoding);

      byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(binaryValue);

      String stringValue;
      try {
         stringValue = new String(copiedBytes, characterEncoding.name());

         final Character terminationCharacter = desc.getFieldProperties().getTerminationCharacter();

         if (terminationCharacter != null) {
            int index = stringValue.indexOf(terminationCharacter, 0);

            if (index != -1)
               stringValue = stringValue.substring(0, index);
         }

         // CONFIG_CHECK: Check for supported encodings
         return stringValue;
      } catch (UnsupportedEncodingException e) {
         throw new BinaryValueConversionException(
            "String conversion failed due to unsupported character encoding <" + characterEncoding + ">.", e, desc,
            binaryValue, byteOrder, characterEncoding);
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractBaseFieldConverter#convertInterpretedToBinary(java.lang.Object,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected ByteBuffer convertInterpretedToBinary(String interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) throws InterpretedValueConversionException {

      // CONFIG_CHECK: Check for supported Encodings
      try {
         return ByteBuffer.wrap(interpretedValue.getBytes(characterEncoding.name()));
      } catch (UnsupportedEncodingException e) {
         throw new InterpretedValueConversionException(
            "String conversion failed due to unsupported character encoding <" + characterEncoding + ">.", e, desc,
            interpretedValue, byteOrder, characterEncoding);
      }
   }
}
