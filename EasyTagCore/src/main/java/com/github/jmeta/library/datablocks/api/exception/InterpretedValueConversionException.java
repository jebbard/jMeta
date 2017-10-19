/**
 *
 * {@link BinaryValueConversionException}.java
 *
 * @author Jens Ebert
 *
 * @date 20.06.2011
 */
package com.github.jmeta.library.datablocks.api.exception;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;

/**
 * {@link InterpretedValueConversionException}
 *
 */
public class InterpretedValueConversionException extends Exception {

   private static final long serialVersionUID = 1L;

   /**
    * Creates a new {@link InterpretedValueConversionException}.
    * 
    * @param message
    * @param cause
    * @param fieldDesc
    * @param interpretedValue
    * @param byteOrder
    * @param characterEncoding
    */
   public InterpretedValueConversionException(String message, Throwable cause,
      DataBlockDescription fieldDesc, Object interpretedValue,
      ByteOrder byteOrder, Charset characterEncoding) {
      super(message, cause);

      m_interpretedValue = interpretedValue;
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
    * @return the interpreted value
    */
   public Object getInterpretedValue() {

      return m_interpretedValue;
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

   private final Object m_interpretedValue;

   private final Charset m_characterEncoding;

   private final ByteOrder m_byteOrder;
}
