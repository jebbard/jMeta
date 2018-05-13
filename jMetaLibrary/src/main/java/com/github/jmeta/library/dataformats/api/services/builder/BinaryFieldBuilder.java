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
 * {@link BinaryFieldBuilder}
 *
 */
public interface BinaryFieldBuilder<ParentBuilder> extends FieldBuilder<ParentBuilder, byte[]>,
   FieldDescriptionModifier<ParentBuilder, byte[], BinaryFieldBuilder<ParentBuilder>>,
   DataBlockDescriptionModifier<BinaryFieldBuilder<ParentBuilder>> {
}
