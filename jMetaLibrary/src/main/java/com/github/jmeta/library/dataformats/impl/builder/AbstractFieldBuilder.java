/**
 *
 * {@link AbstractFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldBuilder} is the base class for all {@link DataBlockDescriptionBuilder}s that build fields. As
 * such, it holds all attributes necessary for creating {@link FieldProperties}.
 *
 * @param <P>
 *           The parent type of this builder
 * @param <FIT>
 *           The interpreted type of the concrete field built by this class
 * @param <C>
 *           The concrete derived interface of the class implementing this
 *           {@link AbstractDataFormatSpecificationBuilder}
 */
public abstract class AbstractFieldBuilder<P extends DataBlockDescriptionBuilder<P>, FIT, C extends FieldBuilder<P, FIT, C>>
   extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldBuilder<P, FIT, C> {

   private Character terminationCharacter;
   private FIT defaultValue;
   private FlagSpecification flagSpecification;
   private Charset fixedCharset;
   private ByteOrder fixedByteOrder;
   private boolean isMagicKey = false;
   private final Map<FIT, byte[]> enumeratedValues = new HashMap<>();
   private final FieldType<FIT> fieldType;
   private final List<FieldFunction> functions = new ArrayList<>();
   private long magicKeyBitLength = DataBlockDescription.UNDEFINED;
   private FieldConverter<FIT> customConverter;

   /**
    * Creates a new {@link AbstractFieldBuilder}.
    * 
    * @param parentBuilder
    *           The parent {@link DataFormatBuilder}. Required for allowing a fluent API, as it is returned by the
    *           {@link #finish()} method. Must not be null.
    * @param localId
    *           The local id of the data block. Must not be null and must not contain the
    *           {@link DataBlockId#SEGMENT_SEPARATOR}.
    * @param name
    *           The human-readable name of the data block in its specification
    * @param description
    *           The description of the data block from its specification
    * @param fieldType
    *           The {@link FieldType} of the field
    * @param isGeneric
    *           true if it is a generic data block, false otherwise
    */
   public AbstractFieldBuilder(P parentBuilder, String localId, String name, String description,
      FieldType<FIT> fieldType, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD, isGeneric);
      Reject.ifNull(fieldType, "fieldType");

      this.fieldType = fieldType;
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
    */
   @SuppressWarnings("unchecked")
   public C asMagicKey() {
      this.isMagicKey = true;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asMagicKeyWithOddBitLength(byte)
    */
   @Override
   public C asMagicKeyWithOddBitLength(long bitLength) {
      this.magicKeyBitLength = bitLength;
      return asMagicKey();
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asIdOf(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C asIdOf(DataBlockId targetId) {
      Set<DataBlockId> affectedBlocks = Set.of(targetId);

      functions.add(new FieldFunction(FieldFunctionType.ID_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#indicatesPresenceOf(java.lang.String,
    *      int, com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @SuppressWarnings("unchecked")
   @Override
   public C indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockId targetId) {
      Set<DataBlockId> affectedBlocks = Set.of(targetId);

      functions.add(new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedBlocks, withFlagName, withFlagValue));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @SuppressWarnings("unchecked")
   @Override
   public C asSizeOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asCountOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @SuppressWarnings("unchecked")
   @Override
   public C asCountOf(DataBlockId targetId) {
      Set<DataBlockId> affectedBlocks = Set.of(targetId);

      functions.add(new FieldFunction(FieldFunctionType.COUNT_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asByteOrderOf(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C asByteOrderOf(DataBlockId targetId) {
      Set<DataBlockId> affectedBlocks = Set.of(targetId);

      functions.add(new FieldFunction(FieldFunctionType.BYTE_ORDER_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asCharacterEncodingOf(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C asCharacterEncodingOf(DataBlockId targetId) {
      Set<DataBlockId> affectedBlocks = Set.of(targetId);

      functions.add(new FieldFunction(FieldFunctionType.CHARACTER_ENCODING_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#addEnumeratedValue(byte[],
    *      java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C addEnumeratedValue(byte[] binaryValue, FIT interpretedValue) {
      enumeratedValues.put(interpretedValue, binaryValue);
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withCustomConverter(com.github.jmeta.library.dataformats.api.types.converter.FieldConverter)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withCustomConverter(FieldConverter<FIT> customConverter) {
      Reject.ifNull(customConverter, "customConverter");
      this.customConverter = customConverter;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder#withDefaultValue(byte[])
    */
   @SuppressWarnings("unchecked")
   public C withDefaultValue(FIT value) {
      this.defaultValue = value;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withLengthOf(long,
    *      long)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLengths(minimumByteLength, maximumByteLength);
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withStaticLengthOf(long)
    */
   @Override
   public C withStaticLengthOf(long staticByteLength) {
      return withLengthOf(staticByteLength, staticByteLength);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#finishField()
    */
   @Override
   public P finishField() {
      FieldProperties<FIT> fieldProperties = new FieldProperties<>(fieldType, defaultValue, enumeratedValues,
         terminationCharacter, flagSpecification, fixedCharset, fixedByteOrder, functions, isMagicKey,
         magicKeyBitLength, customConverter);

      setFieldProperties(fieldProperties);

      return finish();
   }

   protected void setFlagSpecification(FlagSpecification flagSpecification) {
      this.flagSpecification = flagSpecification;
   }

   protected void setTerminationCharacter(Character terminationCharacter) {
      this.terminationCharacter = terminationCharacter;
   }

   protected void setFixedCharset(Charset fixedCharset) {
      this.fixedCharset = fixedCharset;
   }

   protected void setFixedByteOrder(ByteOrder fixedByteOrder) {
      this.fixedByteOrder = fixedByteOrder;
   }
}
