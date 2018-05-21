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

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldBuilder}
 *
 */
public abstract class AbstractFieldBuilder<P extends DataFormatSpecificationBuilder, C extends DataFormatSpecificationBuilder, FieldInterpretedType>
   extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldBuilder<P, FieldInterpretedType> {

   private Character terminationCharacter;
   private FieldInterpretedType defaultValue;
   private final Map<FieldInterpretedType, byte[]> enumeratedValues = new HashMap<>();
   private FlagSpecification flagSpecification;
   private final FieldType<FieldInterpretedType> fieldType;
   private Charset fixedCharset;
   private ByteOrder fixedByteOrder;
   private final List<FieldFunction> functions = new ArrayList<>();
   private boolean isMagicKey = false;

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
      FieldType<FieldInterpretedType> fieldType, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD, isGeneric);
      Reject.ifNull(fieldType, "fieldType");

      this.fieldType = fieldType;
   }

   public C withFieldFunction(FieldFunction function) {
      functions.add(function);
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder#withDefaultValue(byte[])
    */
   public C withDefaultValue(FieldInterpretedType value) {
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
      FieldProperties<FieldInterpretedType> fieldProperties = new FieldProperties<>(fieldType, defaultValue,
         enumeratedValues, terminationCharacter, flagSpecification, fixedCharset, fixedByteOrder, functions,
         isMagicKey);

      setFieldProperties(fieldProperties);

      return finish();
   }

   /**
    * Returns the attribute {@link #enumeratedValues}.
    * 
    * @return the attribute {@link #enumeratedValues}
    */
   protected Map<FieldInterpretedType, byte[]> getEnumeratedValues() {
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
