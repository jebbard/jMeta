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
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO writeTests001: Test failing conversion when Enum interpr. value is unknown
// TODO writeTests002: Test failing conversion when Numeric interpr. value > specified static field size
// TODO writeTests003: Test failing conversion when SyncSafe interpr. value > 2^28
// TODO writeTests004: Test conversion of ANY fields: No converter found

// TODO stage2_011: Test cases for specification validation:
// - Flag bytes length != field static length
// - Numeric field length > 8
// - String field: Unsupported encoding (Spec default and enumerated value)

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
public class StandardField<T> implements Field<T> {

   private int sequenceNumber;
   private ContainerContext containerContext;

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSequenceNumber()
    */
   @Override
   public int getSequenceNumber() {
      return 0;
   }

   private StandardField(DataBlockDescription fieldDesc, MediumOffset reference, int sequenceNumber,
      ContainerContext containerContext) {
      Reject.ifNull(fieldDesc, "fieldDesc");

      m_desc = fieldDesc;
      this.sequenceNumber = sequenceNumber;
      this.containerContext = containerContext;
      m_mediumReference = reference;
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
      ContainerContext containerContext) {
      this(fieldDesc, reference, sequenceNumber, containerContext);

      Reject.ifNull(interpretedValue, "interpretedValue");

      m_interpretedValue = interpretedValue;
      m_totalSize = DataBlockDescription.UNDEFINED;
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
      int sequenceNumber, ContainerContext containerContext) {
      this(fieldDesc, reference, sequenceNumber, containerContext);

      Reject.ifNull(byteValue, "byteValue");

      m_byteValue = byteValue;
      m_totalSize = byteValue.remaining();
   }

   @Override
   public String getStringRepresentation() throws BinaryValueConversionException {

      return getInterpretedValue().toString();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getBytes(MediumOffset, int)
    */
   @Override
   public ByteBuffer getBytes(MediumOffset offset, int size) {
      Reject.ifNegative(size, "size");
      Reject.ifNull(offset, "offset");
      Reject.ifTrue(offset.before(getOffset()), "offset.before(getOffset())");
      Reject.ifFalse(offset.getAbsoluteMediumOffset() + size <= getOffset().getAbsoluteMediumOffset() + getSize(),
         "offset.getAbsoluteMediumOffset() + size <= getOffset().getAbsoluteMediumOffset() + getSize()");

      ByteBuffer subBytes = ByteBuffer.allocate(size);

      ByteBuffer value;
      try {
         value = getBinaryValue();
         int relativeOffset = (int) (offset.getAbsoluteMediumOffset() - getOffset().getAbsoluteMediumOffset());

         for (int currentIndex = relativeOffset; currentIndex < relativeOffset + size; currentIndex++) {
            subBytes.put(value.get(value.position() + currentIndex));
         }
      } catch (InterpretedValueConversionException e) {
         // TODO writeConcept003: log conversion error or return null?
         assert e != null;
         return null;
      }

      subBytes.rewind();

      return subBytes;
   }

   @Override
   public ByteBuffer getBinaryValue() throws InterpretedValueConversionException {

      if (m_byteValue == null) {
         m_byteValue = convertToBinary();
         m_totalSize = m_byteValue.remaining();
      }

      return m_byteValue;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getOffset()
    */
   @Override
   public MediumOffset getOffset() {

      return m_mediumReference;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getParent()
    */
   @Override
   public DataBlock getParent() {

      return m_parent;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getId()
    */
   @Override
   public DataBlockId getId() {

      return m_desc.getId();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSize()
    */
   @Override
   public long getSize() {

      return m_totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#initParent(com.github.jmeta.library.datablocks.api.types.DataBlock)
    */
   @Override
   public void initParent(DataBlock parent) {

      Reject.ifNull(parent, "parent");
      Reject.ifFalse(getParent() == null, "getParent() == null");

      m_parent = parent;
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

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getContainerContext()
    */
   @Override
   public ContainerContext getContainerContext() {
      return containerContext;
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

   private MediumOffset m_mediumReference;

   private long m_totalSize;

   private ByteBuffer m_byteValue;

   private T m_interpretedValue;
}
