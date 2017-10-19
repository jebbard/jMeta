/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlock;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.export.FieldFunctionStack;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.FieldType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.media.api.IMediaAPI;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Reject;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class StandardDataBlockFactory implements IExtendedDataBlockFactory {

   /**
    * @see de.je.jmeta.datablocks.export.IExtendedDataBlockFactory#createContainer(de.je.jmeta.dataformats.DataBlockId,
    *      de.je.jmeta.datablocks.IDataBlock, IMediumReference, java.util.List, de.je.jmeta.datablocks.IPayload,
    *      java.util.List)
    */
   @Override
   public IContainer createContainer(DataBlockId id, IDataBlock parent,
      IMediumReference reference, List<IHeader> headers, IPayload payload,
      List<IHeader> footers) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(footers, "footers");
      Reject.ifNull(payload, "payload");
      Reject.ifNull(headers, "headers");
      return new StandardContainer(id, parent, reference, headers, payload,
         footers, m_dataBlockReader);
   }

   /**
    * @see de.je.jmeta.datablocks.export.IExtendedDataBlockFactory#createFieldFromBytes(DataBlockId,
    *      IDataFormatSpecification, IMediumReference, BinaryValue, ByteOrder, Charset)
    */
   @Override
   public <T> IField<T> createFieldFromBytes(DataBlockId id,
      IDataFormatSpecification spec, IMediumReference reference,
      BinaryValue fieldBytes, ByteOrder byteOrder, Charset characterEncoding) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(reference, "reference");
      Reject.ifNegative(fieldBytes.getTotalSize(),
         "fieldBytes.getTotalSize()");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      @SuppressWarnings("unchecked")
      StandardField<T> field = new StandardField<>(desc, fieldBytes, reference,
         (IFieldConverter<T>) getFieldConverter(id));

      field.initByteOrder(byteOrder);
      field.initCharacterEncoding(characterEncoding);

      return field;
   }

   /**
    * @see de.je.jmeta.datablocks.export.IExtendedDataBlockFactory#createPayloadAfterRead(de.je.jmeta.dataformats.DataBlockId,
    *      IMediumReference, long, de.je.jmeta.datablocks.export.IDataBlockReader, FieldFunctionStack)
    */
   @Override
   public IPayload createPayloadAfterRead(DataBlockId id,
      IMediumReference reference, long totalSize, IDataBlockReader reader,
      FieldFunctionStack context) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(reader, "reader");
      return new LazyPayload(id, reference, totalSize, reader, context);
   }

   /**
    * @see de.je.jmeta.datablocks.export.IExtendedDataBlockFactory#createHeader(de.je.jmeta.dataformats.DataBlockId,
    *      IMediumReference, java.util.List, boolean)
    */
   @Override
   public IHeader createHeader(DataBlockId id, IMediumReference reference,
      List<IField<?>> fields, boolean isFooter) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      return new StandardHeader(id, reference, fields, isFooter,
         m_dataBlockReader);
   }

   /**
    * @see de.je.jmeta.datablocks.export.IExtendedDataBlockFactory#setDataBlockReader(de.je.jmeta.datablocks.export.IDataBlockReader)
    */
   @Override
   public void setDataBlockReader(IDataBlockReader dataBlockReader) {

      Reject.ifNull(dataBlockReader, "dataBlockReader");

      m_dataBlockReader = dataBlockReader;
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlockFactory#createPayload(de.je.jmeta.dataformats.DataBlockId,
    *      de.je.jmeta.datablocks.IDataBlock, java.util.List, java.util.List)
    */
   @Override
   public IPayload createPayload(DataBlockId id, IDataBlock parent,
      List<IContainer> containers, List<IField<?>> fields) {

      return null;
   }

   /**
    * @see IExtendedDataBlockFactory#setMediumFactory
    */
   @Override
   public void setMediumFactory(IMediaAPI mediumFactory) {

      Reject.ifNull(mediumFactory, "mediumFactory");

      m_mediumFactory = mediumFactory;
   }

   @Override
   public <T> IField<T> createFieldForWriting(DataBlockId fieldId, T value) {

      Reject.ifNull(fieldId, "fieldId");
      Reject.ifNull(value, "value");

      DataBlockDescription desc = m_dataBlockReader.getSpecification()
         .getDataBlockDescription(fieldId);

      return new StandardField<>(desc, value, null,
         this.<T> getFieldConverter(fieldId));
   }

   /**
    * @param fieldId
    * @return the {@link IFieldConverter}
    */
   @SuppressWarnings("unchecked")
   protected <T> IFieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      IDataFormatSpecification spec = m_dataBlockReader.getSpecification();

      DataBlockDescription desc = spec.getDataBlockDescription(fieldId);

      return (IFieldConverter<T>) FIELD_CONVERTERS
         .get(desc.getFieldProperties().getFieldType());
   }

   @SuppressWarnings("unused")
   private IMediaAPI m_mediumFactory;

   private IDataBlockReader m_dataBlockReader;

   /**
    *
    */
   protected final static Map<FieldType<?>, IFieldConverter<?>> FIELD_CONVERTERS = new HashMap<>();

   static {
      FIELD_CONVERTERS.put(FieldType.BINARY, new BinaryFieldConverter());
      FIELD_CONVERTERS.put(FieldType.ENUMERATED,
         new EnumeratedFieldConverter());
      FIELD_CONVERTERS.put(FieldType.FLAGS, new FlagsFieldConverter());
      FIELD_CONVERTERS.put(FieldType.UNSIGNED_WHOLE_NUMBER,
         new UnsignedNumericFieldConverter());
      FIELD_CONVERTERS.put(FieldType.STRING, new StringFieldConverter());
   }
}
