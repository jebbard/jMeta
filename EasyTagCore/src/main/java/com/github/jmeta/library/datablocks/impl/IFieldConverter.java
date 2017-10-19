/**
 *
 * {@link IConverter}.java
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
