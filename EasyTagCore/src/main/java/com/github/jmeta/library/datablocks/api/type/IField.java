
package com.github.jmeta.library.datablocks.api.type;

import com.github.jmeta.library.datablocks.api.exception.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exception.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.type.BinaryValue;

/**
 * {@link IField}
 *
 * @param <T>
 */
public interface IField<T> extends IDataBlock {

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
   public BinaryValue getBinaryValue()
      throws InterpretedValueConversionException;

   /**
    * @return the string representation
    * @throws BinaryValueConversionException
    */
   public String getStringRepresentation()
      throws BinaryValueConversionException;
}