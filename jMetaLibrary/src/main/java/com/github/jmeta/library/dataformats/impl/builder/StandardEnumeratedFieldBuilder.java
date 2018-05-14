/**
 *
 * {@link StandardStringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardEnumeratedFieldBuilder}
 *
 */
public class StandardEnumeratedFieldBuilder<ParentBuilder extends DataFormatSpecificationBuilder, FieldInterpretedType>
   extends AbstractFieldBuilder<ParentBuilder, FieldInterpretedType>
   implements EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> {

   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withFieldFunction(FieldFunction function) {
      getFunctions().add(function);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder#withDefaultValue(java.lang.Object)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withDefaultValue(FieldInterpretedType value) {
      setDefaultValue(value);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> asMagicKey() {
      setAsMagicKey();
      return this;
   }

   /**
    * Creates a new {@link StandardEnumeratedFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param fieldType
    */
   public StandardEnumeratedFieldBuilder(ParentBuilder parentBuilder, String localId, String name, String description) {
      super(parentBuilder, localId, name, description, (FieldType<FieldInterpretedType>) FieldType.ENUMERATED);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder#addEnumeratedValue(byte[],
    *      java.lang.Object)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> addEnumeratedValue(byte[] binaryValue,
      FieldInterpretedType interpretedValue) {
      getEnumeratedValues().put(interpretedValue, binaryValue);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOverriddenId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withStaticLengthOf(long)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withLengthOf(long,
    *      long)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withLengthOf(long minimumByteLength,
      long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionModifier#withOccurrences(int,
    *      int)
    */
   @Override
   public EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> withOccurrences(int minimumOccurrences,
      int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return this;
   }

}
