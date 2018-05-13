/**
 *
 * {@link FieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FieldBuilder}
 *
 */
public interface FieldBuilder<ParentBuilder, FieldInterpretedType> extends DataFormatSpecificationBuilder {

   void withDefaultValue(FieldInterpretedType value);

   void asMagicKey();

   ParentBuilder finishField();
}
