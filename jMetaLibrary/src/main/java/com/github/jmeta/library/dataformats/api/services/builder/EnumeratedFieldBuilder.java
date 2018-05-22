/**
 *
 * {@link StringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link EnumeratedFieldBuilder}
 *
 */
public interface EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> extends
   FieldBuilder<ParentBuilder, FieldInterpretedType, EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType>>,
   DataBlockDescriptionModifier<EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType>> {

   EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType> addEnumeratedValue(byte[] binaryValue,
      FieldInterpretedType interpretedValue);

}
