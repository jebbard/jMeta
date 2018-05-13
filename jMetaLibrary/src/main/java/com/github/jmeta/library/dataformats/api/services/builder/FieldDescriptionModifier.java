/**
 *
 * {@link FieldPropertyHolder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link FieldPropertyHolder}
 *
 */
public interface FieldDescriptionModifier<ParentBuilder, FieldInterpretedType, ConcreteFieldBuilder extends FieldBuilder<ParentBuilder, FieldInterpretedType>> {

   ConcreteFieldBuilder withDefaultValue(FieldInterpretedType value);

   ConcreteFieldBuilder asMagicKey();
}
