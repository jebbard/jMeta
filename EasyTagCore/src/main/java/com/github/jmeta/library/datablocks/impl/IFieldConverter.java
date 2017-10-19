/**
 *
 * {@link IConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exception.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exception.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.type.BinaryValue;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;

/**
 * {@link IFieldConverter}
 *
 * @param <T>
 */
public interface IFieldConverter<T> {

   /**
    * @param binaryValue
    * @param desc
    * @param byteOrder
    * @param characterEncoding
    * @return the interpreted value
    * @throws BinaryValueConversionException
    */
   public T toInterpreted(BinaryValue binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException;

   /**
    * @param interpretedValue
    * @param desc
    * @param byteOrder
    * @param characterEncoding
    * @return the binary value
    * @throws InterpretedValueConversionException
    */
   public BinaryValue toBinary(T interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException;
}
