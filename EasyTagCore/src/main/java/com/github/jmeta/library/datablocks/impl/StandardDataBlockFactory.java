/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.IContainer;
import com.github.jmeta.library.datablocks.api.types.IDataBlock;
import com.github.jmeta.library.datablocks.api.types.IField;
import com.github.jmeta.library.datablocks.api.types.IHeader;
import com.github.jmeta.library.datablocks.api.types.IPayload;
import com.github.jmeta.library.dataformats.api.services.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.media.api.services.IMediaAPI;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class StandardDataBlockFactory implements IExtendedDataBlockFactory {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory#createContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.IDataBlock, IMediumReference, java.util.List, com.github.jmeta.library.datablocks.api.types.IPayload,
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
    * @see com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory#createFieldFromBytes(DataBlockId,
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
    * @see com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory#createPayloadAfterRead(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      IMediumReference, long, com.github.jmeta.library.datablocks.api.services.IDataBlockReader, FieldFunctionStack)
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
    * @see com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory#createHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
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
    * @see com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory#setDataBlockReader(com.github.jmeta.library.datablocks.api.services.IDataBlockReader)
    */
   @Override
   public void setDataBlockReader(IDataBlockReader dataBlockReader) {

      Reject.ifNull(dataBlockReader, "dataBlockReader");

      m_dataBlockReader = dataBlockReader;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockFactory#createPayload(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.IDataBlock, java.util.List, java.util.List)
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
