
package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;

/**
 * {@link Field}
 *
 * @param <T>
 */
public interface Field<T> extends DataBlock {

   /**
    * Returns interpretedValue
    *
    * @return interpretedValue
    * @throws BinaryValueConversionException
    */
   public T getInterpretedValue() throws BinaryValueConversionException;

   /**
    * @return the binary value
    * @throws InterpretedValueConversionException
    */
   public ByteBuffer getBinaryValue() throws InterpretedValueConversionException;

   /**
    * Returns interpretedValue
    *
    * @return interpretedValue
    * @throws BinaryValueConversionException
    */
   public void setInterpretedValue(T interpretedBalue);

   /**
    * @return the binary value
    * @throws InterpretedValueConversionException
    */
   public void setBinaryValue(ByteBuffer binaryValue);
}