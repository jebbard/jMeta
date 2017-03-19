/**
 *
 * {@link BinaryValueConversionException}.java
 *
 * @author Jens Ebert
 *
 * @date 20.06.2011
 */
package de.je.jmeta.datablocks;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;

/**
 * {@link BinaryValueConversionException}
 *
 */
public class BinaryValueConversionException extends Exception {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link BinaryValueConversionException}.
    * 
    * @param message
    * @param cause
    * @param fieldDesc
    * @param binaryValue
    * @param byteOrder
    * @param characterEncoding
    */
   public BinaryValueConversionException(String message, Throwable cause,
      DataBlockDescription fieldDesc, BinaryValue binaryValue,
      ByteOrder byteOrder, Charset characterEncoding) {
      super(message, cause);

      m_binaryValue = binaryValue;
      m_characterEncoding = characterEncoding;
      m_byteOrder = byteOrder;
      m_fieldDescription = fieldDesc;
   }

   /**
    * @return the {@link DataBlockDescription}
    */
   public DataBlockDescription getFieldDescription() {

      return m_fieldDescription;
   }

   /**
    * @return the {@link BinaryValue}
    */
   public BinaryValue getBinaryValue() {

      return m_binaryValue;
   }

   /**
    * @return the {@link Charset}
    */
   public Charset getCharacterEncoding() {

      return m_characterEncoding;
   }

   /**
    * @return the {@link ByteOrder}
    */
   public ByteOrder getByteOrder() {

      return m_byteOrder;
   }

   private final DataBlockDescription m_fieldDescription;

   private final BinaryValue m_binaryValue;

   private final Charset m_characterEncoding;

   private final ByteOrder m_byteOrder;
}
