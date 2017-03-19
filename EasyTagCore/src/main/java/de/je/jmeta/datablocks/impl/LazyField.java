/**
 *
 * {@link LazyField}.java
 *
 * @author Jens Ebert
 *
 * @date 12.02.2011
 */
package de.je.jmeta.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.IDataBlock;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.datablocks.export.AbstractDataBlock;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link LazyField}
 *
 */
public class LazyField extends AbstractDataBlock implements IField<Object> {

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
   public LazyField(DataBlockDescription fieldDesc, IMediumReference reference,
      IDataBlock parent, long totalSize, IExtendedDataBlockFactory factory,
      IDataBlockReader dataBlockReader, ByteOrder byteOrder,
      Charset characterEncoding) {
      super(fieldDesc.getId(), parent, reference, dataBlockReader);

      Reject.ifNull(factory, "factory");
      Reject.ifNull(fieldDesc, "fieldDesc");
      Contract.checkPrecondition(totalSize >= 0, "totalSize >= 0");

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
   public void convert(IDataFormatSpecification spec, ByteOrder byteOrder,
      Charset characterEncoding, long fieldByteCount) {

      // Do nothing. Wrapped fields are first converted when getInterpretedValue is called
   }

   /**
    * @see de.je.jmeta.datablocks.IField#getInterpretedValue()
    */
   @Override
   public Object getInterpretedValue() throws BinaryValueConversionException {

      lazilyReadField();

      return m_wrappedField.getInterpretedValue();
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getTotalSize()
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

         IMediumReference mediumReference = getMediumReference();

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

   private IField<Object> m_wrappedField = null;

   private final DataBlockDescription m_fieldDesc;

   private final long m_totalSize;

   private final IExtendedDataBlockFactory m_dbFactory;

   private final ByteOrder m_byteOrder;

   private final Charset m_characterEncoding;
}
