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
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldBuilder}
 *
 */
public abstract class AbstractFieldBuilder<P extends DataBlockDescriptionBuilder<P>, FIT, C extends FieldBuilder<P, FIT, C>>
   extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldBuilder<P, FIT, C> {

   private Character terminationCharacter;
   private FIT defaultValue;
   private final Map<FIT, byte[]> enumeratedValues = new HashMap<>();
   private FlagSpecification flagSpecification;
   private final FieldType<FIT> fieldType;
   private Charset fixedCharset;
   private ByteOrder fixedByteOrder;
   private final List<FieldFunction> functions = new ArrayList<>();
   private boolean isMagicKey = false;

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asIdOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C asIdOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.ID_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#indicatesPresenceOf(java.lang.String,
    *      int, com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C indicatesPresenceOf(String withFlagName, int withFlagValue, DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.ID_OF, affectedBlocks, withFlagName, withFlagValue));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C asSizeOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asCountOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C asCountOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.COUNT_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asByteOrderOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C asByteOrderOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.BYTE_ORDER_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asCharacterEncodingOf(com.github.jmeta.library.dataformats.api.types.DataBlockId[])
    */
   @Override
   public C asCharacterEncodingOf(DataBlockId... ids) {
      Set<DataBlockId> affectedBlocks = Set.of(ids);

      functions.add(new FieldFunction(FieldFunctionType.CHARACTER_ENCODING_OF, affectedBlocks, null, null));

      return (C) this;
   }

   /**
    * Creates a new {@link AbstractFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param type
    */
   public AbstractFieldBuilder(P parentBuilder, String localId, String name, String description,
      FieldType<FIT> fieldType, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD, isGeneric);
      Reject.ifNull(fieldType, "fieldType");

      this.fieldType = fieldType;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder#withDefaultValue(byte[])
    */
   public C withDefaultValue(FIT value) {
      this.defaultValue = value;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
    */
   public C asMagicKey() {
      this.isMagicKey = true;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#finishField()
    */
   @Override
   public P finishField() {
      FieldProperties<FIT> fieldProperties = new FieldProperties<>(fieldType, defaultValue, enumeratedValues,
         terminationCharacter, flagSpecification, fixedCharset, fixedByteOrder, functions, isMagicKey);

      setFieldProperties(fieldProperties);

      return finish();
   }

   /**
    * Returns the attribute {@link #enumeratedValues}.
    * 
    * @return the attribute {@link #enumeratedValues}
    */
   protected Map<FIT, byte[]> getEnumeratedValues() {
      return enumeratedValues;
   }

   /**
    * Sets the attribute {@link #flagSpecification}.
    *
    * @param new
    *           vakue for attribute {@link #flagSpecification flagSpecification}.
    */
   protected void setFlagSpecification(FlagSpecification flagSpecification) {
      this.flagSpecification = flagSpecification;
   }

   /**
    * Sets the attribute {@link #fixedCharset}.
    *
    * @param new
    *           vakue for attribute {@link #fixedCharset fixedCharset}.
    */
   protected void setFixedCharset(Charset fixedCharset) {
      this.fixedCharset = fixedCharset;
   }

   protected void setTerminationCharacter(Character terminationCharacter) {
      this.terminationCharacter = terminationCharacter;
   }

   /**
    * Sets the attribute {@link #fixedByteOrder}.
    *
    * @param new
    *           vakue for attribute {@link #fixedByteOrder fixedByteOrder}.
    */
   protected void setFixedByteOrder(ByteOrder fixedByteOrder) {
      this.fixedByteOrder = fixedByteOrder;
   }

}
