
package com.github.jmeta.library.datablocks.api.types;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;

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