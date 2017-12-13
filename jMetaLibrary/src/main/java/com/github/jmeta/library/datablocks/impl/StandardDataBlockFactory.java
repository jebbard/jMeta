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

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
@SuppressWarnings("rawtypes")
public class StandardDataBlockFactory implements ExtendedDataBlockFactory {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock, MediumOffset, java.util.List, com.github.jmeta.library.datablocks.api.types.Payload,
    *      java.util.List)
    */
   @Override
   public Container createContainer(DataBlockId id, DataBlock parent,
      MediumOffset reference, List<Header> headers, Payload payload,
      List<Header> footers) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(footers, "footers");
      Reject.ifNull(payload, "payload");
      Reject.ifNull(headers, "headers");
      return new StandardContainer(id, parent, reference, headers, payload,
         footers, m_dataBlockReader);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createFieldFromBytes(DataBlockId,
    *      DataFormatSpecification, MediumOffset, BinaryValue, ByteOrder, Charset)
    */
   @Override
   public <T> Field<T> createFieldFromBytes(DataBlockId id,
      DataFormatSpecification spec, MediumOffset reference,
      BinaryValue fieldBytes, ByteOrder byteOrder, Charset characterEncoding) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(reference, "reference");
      Reject.ifNegative(fieldBytes.getTotalSize(),
         "fieldBytes.getTotalSize()");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      @SuppressWarnings("unchecked")
      StandardField<T> field = new StandardField<>(desc, fieldBytes, reference,
         (FieldConverter<T>) getFieldConverter(id));

      field.initByteOrder(byteOrder);
      field.initCharacterEncoding(characterEncoding);

      return field;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPayloadAfterRead(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, long, com.github.jmeta.library.datablocks.api.services.DataBlockReader, FieldFunctionStack)
    */
   @Override
   public Payload createPayloadAfterRead(DataBlockId id,
      MediumOffset reference, long totalSize, DataBlockReader reader,
      FieldFunctionStack context) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(reader, "reader");
      return new LazyPayload(id, reference, totalSize, reader, context);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, java.util.List, boolean)
    */
   @Override
   public Header createHeader(DataBlockId id, MediumOffset reference,
      List<Field<?>> fields, boolean isFooter) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      return new StandardHeader(id, reference, fields, isFooter,
         m_dataBlockReader);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#setDataBlockReader(com.github.jmeta.library.datablocks.api.services.DataBlockReader)
    */
   @Override
   public void setDataBlockReader(DataBlockReader dataBlockReader) {

      Reject.ifNull(dataBlockReader, "dataBlockReader");

      m_dataBlockReader = dataBlockReader;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockFactory#createPayload(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock, java.util.List, java.util.List)
    */
   @Override
   public Payload createPayload(DataBlockId id, DataBlock parent,
      List<Container> containers, List<Field<?>> fields) {

      return null;
   }

   /**
    * @see ExtendedDataBlockFactory#setMediumFactory
    */
   @Override
   public void setMediumFactory(MediaAPI mediumFactory) {

      Reject.ifNull(mediumFactory, "mediumFactory");

      m_mediumFactory = mediumFactory;
   }

   @Override
   public <T> Field<T> createFieldForWriting(DataBlockId fieldId, T value) {

      Reject.ifNull(fieldId, "fieldId");
      Reject.ifNull(value, "value");

      DataBlockDescription desc = m_dataBlockReader.getSpecification()
         .getDataBlockDescription(fieldId);

      return new StandardField<>(desc, value, null,
         this.<T> getFieldConverter(fieldId));
   }

   /**
    * @param fieldId
    * @return the {@link FieldConverter}
    */
   @SuppressWarnings("unchecked")
   protected <T> FieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      DataFormatSpecification spec = m_dataBlockReader.getSpecification();

      DataBlockDescription desc = spec.getDataBlockDescription(fieldId);

      return (FieldConverter<T>) FIELD_CONVERTERS
         .get(desc.getFieldProperties().getFieldType());
   }

   @SuppressWarnings("unused")
   private MediaAPI m_mediumFactory;

   private DataBlockReader m_dataBlockReader;

   /**
    *
    */
   protected final static Map<FieldType<?>, FieldConverter<?>> FIELD_CONVERTERS = new HashMap<>();

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
