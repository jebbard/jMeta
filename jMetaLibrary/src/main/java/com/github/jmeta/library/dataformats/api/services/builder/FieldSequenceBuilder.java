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
public interface FieldSequenceBuilder<C extends DataBlockDescriptionBuilder<C>> extends DataBlockDescriptionBuilder<C> {

   StringFieldBuilder<C> addStringField(String localId, String name, String description);

   NumericFieldBuilder<C> addNumericField(String localId, String name, String description);

   BinaryFieldBuilder<C> addBinaryField(String localId, String name, String description);

   FlagsFieldBuilder<C> addFlagsField(String localId, String name, String description);

   <FIT> EnumeratedFieldBuilder<C, FIT> addEnumeratedField(Class<FIT> type, String localId, String name,
      String description);

   FieldSequenceBuilder<C> removeField(String localId);
}
