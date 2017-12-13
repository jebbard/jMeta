/**
 *
 * {@link LazyField}.java
 *
 * @author Jens Ebert
 *
 * @date 12.02.2011
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link LazyField}
 *
 */
public class LazyField extends AbstractDataBlock implements Field<Object> {

   @Override
   public String getStringRepresentation()
      throws BinaryValueConversionException {

      return getClass().getName().toUpperCase();
   }

   /**
    * Creates a new {@link LazyField}.
    * 
    * @param fieldDesc
    * @param reference
    * @param parent
    * @param totalSize
    * @param factory
    * @param dataBlockReader
    * @param byteOrder
    * @param characterEncoding
    */
   public LazyField(DataBlockDescription fieldDesc, MediumOffset reference,
      DataBlock parent, long totalSize, ExtendedDataBlockFactory factory,
      DataBlockReader dataBlockReader, ByteOrder byteOrder,
      Charset characterEncoding) {
      super(fieldDesc.getId(), parent, reference, dataBlockReader);

      Reject.ifNull(factory, "factory");
      Reject.ifNull(fieldDesc, "fieldDesc");
      Reject.ifNegative(totalSize, "totalSize");

      m_totalSize = totalSize;
      m_dbFactory = factory;
      m_fieldDesc = fieldDesc;
      m_characterEncoding = characterEncoding;
      m_byteOrder = byteOrder;
   }

   /**
    * @param spec
    * @param byteOrder
    * @param characterEncoding
    * @param fieldByteCount
    */
   public void convert(DataFormatSpecification spec, ByteOrder byteOrder,
      Charset characterEncoding, long fieldByteCount) {

      // Do nothing. Wrapped fields are first converted when getInterpretedValue is called
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Field#getInterpretedValue()
    */
   @Override
   public Object getInterpretedValue() throws BinaryValueConversionException {

      lazilyReadField();

      return m_wrappedField.getInterpretedValue();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      return m_totalSize;
   }

   @Override
   public BinaryValue getBinaryValue()
      throws InterpretedValueConversionException {

      lazilyReadField();

      return m_wrappedField.getBinaryValue();
   }

   private void lazilyReadField() {

      if (m_wrappedField == null) {
         int fragmentCount = (int) m_totalSize / Integer.MAX_VALUE;
         int fragmentRemainder = (int) m_totalSize % Integer.MAX_VALUE;

         if (fragmentRemainder > 0)
            fragmentCount++;

         byte[][] binaryData = new byte[fragmentCount][];

         MediumOffset mediumReference = getMediumReference();

         int fragmentIndex = 0;

         for (; fragmentIndex < fragmentCount - 1; fragmentIndex++) {
            byte[] readData = getDataBlockReader()
               .readBytes(mediumReference, Integer.MAX_VALUE).array();

            binaryData[fragmentIndex] = readData;

            mediumReference = mediumReference.advance(Integer.MAX_VALUE);
         }

         binaryData[fragmentIndex] = getDataBlockReader()
            .readBytes(mediumReference, fragmentRemainder).array();

         m_wrappedField = m_dbFactory.createFieldFromBytes(m_fieldDesc.getId(),
            getDataBlockReader().getSpecification(), mediumReference,
            new BinaryValue(binaryData), m_byteOrder, m_characterEncoding);
      }
   }

   private Field<Object> m_wrappedField = null;

   private final DataBlockDescription m_fieldDesc;

   private final long m_totalSize;

   private final ExtendedDataBlockFactory m_dbFactory;

   private final ByteOrder m_byteOrder;

   private final Charset m_characterEncoding;
}
