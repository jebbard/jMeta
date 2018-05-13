/**
 *
 * {@link FieldSequenceBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FieldSequenceBuilder}
 *
 */
public interface FieldSequenceBuilder<ConcreteFieldSequenceBuilder> extends DataFormatSpecificationBuilder {

   StringFieldBuilder<ConcreteFieldSequenceBuilder> addStringField(String localId, String name, String description);

   NumericFieldBuilder<ConcreteFieldSequenceBuilder> addNumericField(String localId, String name, String description);

   BinaryFieldBuilder<ConcreteFieldSequenceBuilder> addBinaryField(String localId, String name, String description);

   FlagsFieldBuilder<ConcreteFieldSequenceBuilder> addFlagsField(String localId, String name, String description);

   <FieldInterpretedType> EnumeratedFieldBuilder<ConcreteFieldSequenceBuilder, FieldInterpretedType> addEnumeratedField(
      Class<FieldInterpretedType> type, String localId, String name, String description);
}
