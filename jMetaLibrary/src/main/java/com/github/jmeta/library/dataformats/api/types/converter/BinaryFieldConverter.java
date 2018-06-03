/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.library.dataformats.api.types.converter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link BinaryFieldConverter}
 *
 */
public class BinaryFieldConverter extends AbstractBaseFieldConverter<ByteBuffer> {

   /**
    * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertBinaryToInterpreted(java.nio.ByteBuffer,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected ByteBuffer convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) {
      return binaryValue;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertInterpretedToBinary(java.lang.Object,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription, java.nio.ByteOrder,
    *      java.nio.charset.Charset)
    */
   @Override
   protected ByteBuffer convertInterpretedToBinary(ByteBuffer interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding) {
      return interpretedValue;
   }
}
