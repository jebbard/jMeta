/**
 * {@link StandardField}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents a leaf node in the data hierarchy of a {@link AbstractMedium}. A {@link StandardField} usually has a small
 * size and represents a concrete value, e.g. for parsing, descriptive properties or containing raw binary data.
 *
 * For {@link StandardField}s, one must distinguish the raw byte value and the so-called <i>interpreted</i> value. The
 * interpreted value is the value interesting for the user, therefore often human-readable. To convert raw bytes to an
 * interpreted value, specific knowledge about and interpretation of the raw bytes is necessary.
 *
 * Derived classes represent the concrete value types a {@link StandardField} can have.
 *
 * @param <T>
 *           the exact type of interpreted value stored in this {@link StandardField}.
 */
public class StandardField<T> extends AbstractDataBlock implements Field<T> {

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSize()
    */
   @Override
   public long getSize() {

      if (m_byteValue != null) {
         return m_byteValue.remaining();
      }

      return DataBlockDescription.UNDEFINED;
   }

   private StandardField(DataBlockDescription fieldDesc, MediumOffset reference, int sequenceNumber,
      ContainerContext containerContext, DataBlockReader reader, DataBlock parent) {
      super(fieldDesc.getId(), parent, reference, reader, sequenceNumber, containerContext, DataBlockState.PERSISTED);
      Reject.ifNull(fieldDesc, "fieldDesc");

      m_desc = fieldDesc;
      m_fieldConverter = (FieldConverter<T>) fieldDesc.getFieldProperties().getConverter();
      m_byteOrder = containerContext.getByteOrderOf(getId(), sequenceNumber);
      m_characterEncoding = containerContext.getCharacterEncodingOf(getId(), sequenceNumber);
   }

   /**
    * Creates a new {@link StandardField}.
    *
    * @param fieldDesc
    * @param interpretedValue
    * @param reference
    * @param sequenceNumber
    *           TODO
    * @param containerContext
    *           TODO
    */
   public StandardField(DataBlockDescription fieldDesc, T interpretedValue, MediumOffset reference, int sequenceNumber,
      ContainerContext containerContext, DataBlockReader reader) {
      this(fieldDesc, reference, sequenceNumber, containerContext, reader, null);

      Reject.ifNull(interpretedValue, "interpretedValue");

      m_interpretedValue = interpretedValue;
   }

   /**
    * Creates a new {@link StandardField}.
    *
    * @param fieldDesc
    * @param byteValue
    * @param reference
    * @param sequenceNumber
    *           TODO
    * @param containerContext
    *           TODO
    */
   public StandardField(DataBlockDescription fieldDesc, ByteBuffer byteValue, MediumOffset reference,
      int sequenceNumber, ContainerContext containerContext, DataBlockReader reader, DataBlock parent) {
      this(fieldDesc, reference, sequenceNumber, containerContext, reader, parent);

      Reject.ifNull(byteValue, "byteValue");

      m_byteValue = byteValue;
   }

   @Override
   public ByteBuffer getBinaryValue() throws InterpretedValueConversionException {

      if (m_byteValue == null) {
         m_byteValue = convertToBinary();
      }

      return m_byteValue;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Field#getInterpretedValue()
    */
   @Override
   public T getInterpretedValue() throws BinaryValueConversionException {

      if (m_interpretedValue == null) {
         m_interpretedValue = convertToInterpreted();
      }

      return m_interpretedValue;
   }

   /**
    */
   private T convertToInterpreted() throws BinaryValueConversionException {

      if (m_fieldConverter == null) {
         throw new BinaryValueConversionException("No field converter found for field id " + m_desc.getId(), null,
            m_desc, m_byteValue, m_byteOrder, m_characterEncoding);
      }

      if (m_byteOrder == null) {
         throw new BinaryValueConversionException("No byte order set for field id " + m_desc.getId(), null, m_desc,
            m_byteValue, null, null);
      }

      if (m_characterEncoding == null) {
         throw new BinaryValueConversionException("No character encoding set for field id " + m_desc.getId(), null,
            m_desc, m_byteValue, null, null);
      }

      return m_fieldConverter.toInterpreted(m_byteValue, m_desc, m_byteOrder, m_characterEncoding);
   }

   private ByteBuffer convertToBinary() throws InterpretedValueConversionException {

      if (m_fieldConverter == null) {
         throw new InterpretedValueConversionException("No field converter found for field id " + m_desc.getId(), null,
            m_desc, m_interpretedValue, m_byteOrder, m_characterEncoding);
      }

      if (m_byteOrder == null) {
         throw new InterpretedValueConversionException("No byte order set for field id " + m_desc.getId(), null, m_desc,
            m_interpretedValue, null, null);
      }

      if (m_characterEncoding == null) {
         throw new InterpretedValueConversionException("No character encoding set for field id " + m_desc.getId(), null,
            m_desc, m_interpretedValue, null, null);
      }

      return m_fieldConverter.toBinary(m_interpretedValue, m_desc, m_byteOrder, m_characterEncoding);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#setBytes(byte[][])
    */
   @Override
   public void setBytes(byte[][] bytes) {

      // TODO writeConcept002: Implement setBytes for fields
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getSimpleName() + "[id=" + getId().getGlobalId() + ", totalSize=" + getSize()
         + ", m_interpretedValue=" + m_interpretedValue + ", parentId="
         + (getParent() == null ? getParent() : getParent().getId()) + ", medium=" + getOffset() + "]";
   }

   private final FieldConverter<T> m_fieldConverter;

   private DataBlock m_parent;

   private final DataBlockDescription m_desc;

   private Charset m_characterEncoding;

   private ByteOrder m_byteOrder;

   private ByteBuffer m_byteValue;

   private T m_interpretedValue;
}
