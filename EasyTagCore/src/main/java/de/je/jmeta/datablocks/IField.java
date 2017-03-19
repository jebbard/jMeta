
package de.je.jmeta.datablocks;

import de.je.jmeta.dataformats.BinaryValue;

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